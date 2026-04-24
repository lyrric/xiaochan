package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.mapper.LocationMapper;
import io.github.xiaocan.model.dto.LocationDTO;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.vo.LocationVO;
import io.github.xiaocan.service.LocationService;
import io.github.xiaocan.service.MonitoryConfigService;
import io.github.xiaocan.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class LocationServiceImpl extends ServiceImpl<LocationMapper, LocationEntity> implements LocationService {

    @Resource
    private UserService userService;
    @Resource
    private MonitoryConfigService monitoryConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(LocationDTO locationDTO) {
        // 获取当前登录用户
        UserEntity currentUser = userService.getByCurrentRequest();
        // 将DTO转换为Entity
        LocationEntity locationEntity = new LocationEntity();
        BeanUtils.copyProperties(locationDTO, locationEntity);
        locationEntity.setUserId(currentUser.getId());

        // 保存地址
        boolean saved = this.save(locationEntity);
        if (!saved) {
            throw new BusinessException("新增地址失败");
        }

        return locationEntity.getId().intValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        // 获取当前登录用户
        UserEntity currentUser = userService.getByCurrentRequest();


        // 查询地址是否存在
        LocationEntity location = this.getById(id);
        if (location == null) {
            throw new BusinessException( "地址不存在");
        }

        // 权限校验：只能删除自己的地址
        if (!location.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除该地址");
        }
        monitoryConfigService.deleteByLocationId(Integer.parseInt(id));
        // 删除地址
        this.removeById(id);
    }



    @Override
    public List<LocationVO> getAll() {
        // 获取当前登录用户
        UserEntity currentUser = userService.getByCurrentRequest();

        // 查询当前用户的所有地址
        LambdaQueryWrapper<LocationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocationEntity::getUserId, currentUser.getId());
        return this.list(wrapper).stream()
                .map(entity->{
                    LocationVO vo = new LocationVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                }).toList();
    }
}
