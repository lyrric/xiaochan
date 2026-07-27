package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.http.XiaochanHttp;
import io.github.xiaocan.mapper.FavoriteStoreMapper;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.FavoriteStoreQueryDTO;
import io.github.xiaocan.model.dto.RemoveFavoriteDTO;
import io.github.xiaocan.model.dto.SaveFavoriteDTO;
import io.github.xiaocan.model.entity.FavoriteStoreEntity;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.FavoriteStoreVO;
import io.github.xiaocan.service.FavoriteStoreService;
import io.github.xiaocan.service.LocationService;
import io.github.xiaocan.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FavoriteStoreServiceImpl extends ServiceImpl<FavoriteStoreMapper, FavoriteStoreEntity> implements FavoriteStoreService {

    @Resource
    private UserService userService;
    @Resource
    private LocationService locationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFavorite(SaveFavoriteDTO dto) {
        UserEntity currentUser = userService.getByCurrentRequest();
        StoreTypeEnum storeTypeEnum = parseStoreType(dto.getStoreType());

        // 幂等：先删除同用户、同地址、同门店、同类型的收藏
        LambdaQueryWrapper<FavoriteStoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteStoreEntity::getUserId, currentUser.getId())
                .eq(FavoriteStoreEntity::getLocationId, dto.getLocationId())
                .eq(FavoriteStoreEntity::getUniqId, dto.getUniqueId())
                .eq(FavoriteStoreEntity::getStoreType, storeTypeEnum);
        this.remove(wrapper);

        FavoriteStoreEntity entity = new FavoriteStoreEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setUserId(currentUser.getId());
        entity.setStoreType(storeTypeEnum);
        entity.setUniqId(dto.getUniqueId());
        this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(RemoveFavoriteDTO dto) {
        UserEntity currentUser = userService.getByCurrentRequest();
        StoreTypeEnum storeTypeEnum = parseStoreType(dto.getStoreType());

        LambdaQueryWrapper<FavoriteStoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteStoreEntity::getUserId, currentUser.getId())
                .eq(FavoriteStoreEntity::getLocationId, dto.getLocationId())
                .eq(FavoriteStoreEntity::getUniqId, dto.getUniqueId())
                .eq(FavoriteStoreEntity::getStoreType, storeTypeEnum);
        this.remove(wrapper);
    }

    @Override
    public List<FavoriteStoreVO> listFavorites(Long locationId, String storeType) {
        UserEntity currentUser = userService.getByCurrentRequest();
        LambdaQueryWrapper<FavoriteStoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteStoreEntity::getUserId, currentUser.getId());
        if (locationId != null) {
            wrapper.eq(FavoriteStoreEntity::getLocationId, locationId);
        }
        if (StringUtils.hasText(storeType)) {
            wrapper.eq(FavoriteStoreEntity::getStoreType, parseStoreType(storeType));
        }
        wrapper.orderByDesc(FavoriteStoreEntity::getCreateTime);
        return this.list(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public Page<StoreInfo> queryFavoriteStores(FavoriteStoreQueryDTO dto) {
        UserEntity currentUser = userService.getByCurrentRequest();
        Long locationId = dto.getLocationId();
        if (locationId == null) {
            throw new BusinessException("locationId不能为空");
        }
        LocationEntity location = locationService.getById(locationId);
        if (location == null) {
            throw new BusinessException("地址不存在");
        }

        int pageNum = dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        Page<FavoriteStoreEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FavoriteStoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteStoreEntity::getUserId, currentUser.getId())
                .eq(FavoriteStoreEntity::getLocationId, locationId);
        if (StringUtils.hasText(dto.getStoreType())) {
            wrapper.eq(FavoriteStoreEntity::getStoreType, parseStoreType(dto.getStoreType()));
        }
        if (StringUtils.hasText(dto.getStoreName())) {
            wrapper.like(FavoriteStoreEntity::getName, dto.getStoreName());
        }
        wrapper.orderByDesc(FavoriteStoreEntity::getCreateTime);
        this.page(page, wrapper);
        if (page.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        List<StoreInfo> records = new ArrayList<>();
        for (FavoriteStoreEntity favorite : page.getRecords()) {
            List<StoreInfo> matched = queryStoreByFavorite(favorite, location);
            if (matched != null && !matched.isEmpty()) {
                matched.forEach(item -> {
                    item.setFavoriteId(favorite.getId());
                    item.setExists(true);
                });
                records.addAll(matched);
            } else {
                StoreInfo fallback = new StoreInfo();
                BeanUtils.copyProperties(favorite, fallback);
                fallback.setUniqId(favorite.getUniqId());
                fallback.setStoreTypeEnum(favorite.getStoreType());
                fallback.setFavoriteId(favorite.getId());
                fallback.setExists(false);
                records.add(fallback);
            }
        }
        Page<StoreInfo> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    private List<StoreInfo> queryStoreByFavorite(FavoriteStoreEntity favorite, LocationEntity location) {
        StoreTypeEnum storeType = favorite.getStoreType();
        String name = favorite.getName();
        String longitude = location.getLongitude();
        String latitude = location.getLatitude();
        try {
            if (!StringUtils.hasText(name)) {
                return Collections.emptyList();
            }
            if (StoreTypeEnum.XC_MANJIAN.equals(storeType)) {
                List<StoreInfo> list = XiaochanHttp.searchList(name, location.getCityCode(), longitude, latitude, 0, 15);
                return list.stream()
                        .filter(item -> Objects.equals(item.getUniqId(), favorite.getUniqId()))
                        .peek(item -> item.setStoreTypeEnum(StoreTypeEnum.XC_MANJIAN))
                        .toList();
            } else if (StoreTypeEnum.XC_MTSJ.equals(storeType)) {
                List<StoreInfo> meituanList = XiaochanHttp.searchMeituanList(longitude, latitude, name, "").getStoreInfos();
                return meituanList.stream()
                        .filter(item -> Objects.equals(item.getUniqId(), favorite.getUniqId()))
                        .peek(item -> item.setStoreTypeEnum(StoreTypeEnum.XC_MTSJ))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("刷新收藏门店信息失败, favoriteId={}, name={}, storeType={}", favorite.getId(), name, storeType, e);
        }
        return Collections.emptyList();
    }

    private FavoriteStoreVO convertToVO(FavoriteStoreEntity entity) {
        FavoriteStoreVO vo = new FavoriteStoreVO();
        vo.setUniqueId(entity.getUniqId());
        if (entity.getStoreType() != null) {
            vo.setStoreType(entity.getStoreType().name());
        }
        return vo;
    }

    private StoreTypeEnum parseStoreType(String storeType) {
        if (!StringUtils.hasText(storeType)) {
            throw new BusinessException("storeType不能为空");
        }
        try {
            return StoreTypeEnum.valueOf(storeType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("storeType不合法");
        }
    }
}
