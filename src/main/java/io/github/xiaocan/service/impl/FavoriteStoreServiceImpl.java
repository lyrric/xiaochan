package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.mapper.FavoriteStoreMapper;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.*;
import io.github.xiaocan.model.entity.FavoriteStoreEntity;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;
import io.github.xiaocan.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoriteStoreServiceImpl extends ServiceImpl<FavoriteStoreMapper, FavoriteStoreEntity> implements FavoriteStoreService {

    @Resource
    private UserService userService;
    @Resource
    private LocationService locationService;
    @Resource
    private XiaoChanService xiaoChanService;
    @Resource
    @Lazy
    private WmmtService wmmtService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFavorite(SaveFavoriteDTO dto) {
        UserEntity currentUser = userService.getByCurrentRequest();
        StoreTypeEnum storeTypeEnum = dto.getStoreType();

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
        return entity.getId();
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
    @Transactional(rollbackFor = Exception.class)
    public void removeFavoriteById(Long favoriteId) {
        if (favoriteId == null) {
            throw new BusinessException("favoriteId不能为空");
        }
        UserEntity currentUser = userService.getByCurrentRequest();
        LambdaQueryWrapper<FavoriteStoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteStoreEntity::getId, favoriteId)
                .eq(FavoriteStoreEntity::getUserId, currentUser.getId());
        this.remove(wrapper);
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
            List<StoreInfo> storeInfos = Collections.emptyList();
            if (StoreTypeEnum.XC_MANJIAN.equals(storeType)) {
                storeInfos = xiaoChanService.searchList(name, location.getCityCode(), longitude, latitude);
            } else if (StoreTypeEnum.XC_MTSJ.equals(storeType)) {
                XcMeituanshangjinDTO dto = new XcMeituanshangjinDTO();
                dto.setLongitude(longitude);
                dto.setLatitude(latitude);
                dto.setName(name);
                dto.setPvId("");
                storeInfos = xiaoChanService.getXcMeituanshangjinPageVO(dto).getStoreInfos();
            }else if (StoreTypeEnum.WM_MANJIAN.equals(storeType) || StoreTypeEnum.WM_MTSJ.equals(storeType)) {
                WmmtShopListDTO dto = new WmmtShopListDTO();
                dto.setName(name);
                dto.setLongitude(longitude);
                dto.setLatitude(latitude);
                storeInfos = wmmtService.getShopList(dto).getStoreInfos();
            }
            return storeInfos.stream()
                    .filter(item -> Objects.equals(item.getUniqId(), favorite.getUniqId()))
                    .filter(item -> Objects.equals(item.getStoreTypeEnum(), favorite.getStoreType()))
                    .toList();
        } catch (Exception e) {
            log.warn("刷新收藏门店信息失败, favoriteId={}, name={}, storeType={}", favorite.getId(), name, storeType, e);
        }
        return Collections.emptyList();
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

    @Override
    public Map<String, FavoriteStoreEntity> batchQueryFavoriteIds(Integer userId, Long locationId, java.util.Collection<String> uniqIds) {
        if (userId == null || locationId == null || CollectionUtils.isEmpty(uniqIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<FavoriteStoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteStoreEntity::getUserId, userId)
                .eq(FavoriteStoreEntity::getLocationId, locationId)
                .in(FavoriteStoreEntity::getUniqId, uniqIds);
        return this.list(wrapper).stream()
                .collect(Collectors.toMap(FavoriteStoreEntity::getUniqId, e -> e, (a, b) -> a));
    }

    @Override
    public void fillFavoriteIds(List<StoreInfo> storeInfos, Integer userId, Long locationId) {
        if (CollectionUtils.isEmpty(storeInfos) || userId == null || locationId == null) {
            return;
        }
        List<String> uniqIds = storeInfos.stream()
                .map(StoreInfo::getUniqId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (uniqIds.isEmpty()) return;
        Map<String, FavoriteStoreEntity> favMap = batchQueryFavoriteIds(userId, locationId, uniqIds);
        for (StoreInfo store : storeInfos) {
            FavoriteStoreEntity fav = favMap.get(store.getUniqId());
            if (fav != null && Objects.equals(fav.getStoreType(), store.getStoreTypeEnum())) {
                store.setFavoriteId(fav.getId());
            }
        }
    }

    @Override
    public void fillFavoriteIdsForPushedHistory(List<StorePushedHistoryVO> voList, Integer userId) {
        if (CollectionUtils.isEmpty(voList) || userId == null) {
            return;
        }
        // 按locationId分组，每个分组单独查询
        Map<Long, List<StorePushedHistoryVO>> byLocation = voList.stream()
                .filter(vo -> vo.getLocationId() != null)
                .collect(Collectors.groupingBy(StorePushedHistoryVO::getLocationId));
        for (Map.Entry<Long, List<StorePushedHistoryVO>> entry : byLocation.entrySet()) {
            Long locationId = entry.getKey();
            List<StorePushedHistoryVO> group = entry.getValue();
            List<String> uniqIds = group.stream()
                    .map(StorePushedHistoryVO::getUniqId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (uniqIds.isEmpty()) continue;
            Map<String, FavoriteStoreEntity> favMap = batchQueryFavoriteIds(userId, locationId, uniqIds);
            for (StorePushedHistoryVO vo : group) {
                FavoriteStoreEntity fav = favMap.get(vo.getUniqId());
                if (fav != null && Objects.equals(fav.getStoreType(), vo.getStoreTypeEnum())) {
                    vo.setFavoriteId(fav.getId());
                }
            }
        }
    }
}
