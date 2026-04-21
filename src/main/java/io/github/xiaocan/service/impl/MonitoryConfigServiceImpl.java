package io.github.xiaocan.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.mapper.NotifyConfigMapper;
import io.github.xiaocan.model.MinimumPayExtNotifyConfig;
import io.github.xiaocan.model.StoreExtNotifyConfig;
import io.github.xiaocan.model.dto.monitorConfigDTO;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.model.vo.NotifyConfigVO;
import io.github.xiaocan.service.MonitoryConfigService;
import io.github.xiaocan.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现类
 *
 * @author xiaochan
 */
@Slf4j
@Service
public class MonitoryConfigServiceImpl extends ServiceImpl<NotifyConfigMapper, MonitorConfigEntity> implements MonitoryConfigService {

    @Resource
    private UserService userService;

    @Override
    public List<NotifyConfigVO> listByUserId() {
        return this.lambdaQuery()
                .eq(MonitorConfigEntity::getUserId, userService.getByCurrentRequest().getId())
                .list().stream().map(entity->{
                    NotifyConfigVO vo = new NotifyConfigVO();
                    BeanUtils.copyProperties(entity, vo);
                    if (entity.getType() == MonitorTypeEnums.STORE_ACTIVITY) {
                        vo.setStoreExtNotifyConfig(JSONObject.parseObject(entity.getExtConfig(), StoreExtNotifyConfig.class));
                    }else{
                        vo.setMinimumPayExtNotifyConfig(JSONObject.parseObject(entity.getExtConfig(), MinimumPayExtNotifyConfig.class));
                    }
                    return vo;
        }).toList();
    }

    @Override
    public List<MonitorConfigEntity> list(MonitorTypeEnums type, MonitorConfigStatusEnums enums) {
        return this.lambdaQuery()
                .eq(MonitorConfigEntity::getType, type)
                .eq(MonitorConfigEntity::getStatus, enums)
                .list();
    }

    @Override
    public void addUpdateConfig(monitorConfigDTO dto) {
        log.info("保存通知配置请求: {}", dto);
        UserEntity user = userService.getByCurrentRequest();
        MonitorConfigEntity entity;
        if (dto.getId() != null) {
            entity = getById(dto.getId());
            if (entity == null || !entity.getUserId().equals(user.getId())) {
                throw new BusinessException("无权修改该通知配置");
            }
        }else{
            entity = new MonitorConfigEntity();
            entity.setUserId(user.getId());
        }
        BeanUtils.copyProperties(dto, entity);
        if (dto.getType() == MonitorTypeEnums.STORE_ACTIVITY) {
            entity.setExtConfig(JSONObject.toJSONString(dto.getStoreExtNotifyConfig()));
        }else{
            entity.setExtConfig(JSONObject.toJSONString(dto.getMinimumPayExtNotifyConfig()));
        }
        saveOrUpdate(entity);
    }

    @Override
    public void updateConfig(int id, MonitorConfigStatusEnums statusEnums, String remark) {
        this.lambdaUpdate()
                .eq(MonitorConfigEntity::getId, id)
                .set(MonitorConfigEntity::getStatus, statusEnums)
                .set(MonitorConfigEntity::getRemark, remark)
                .update();
    }

    @Override
    public void deleteById(Integer configId) {
        this.lambdaUpdate()
                .eq(MonitorConfigEntity::getId, configId)
                .eq(MonitorConfigEntity::getUserId, userService.getByCurrentRequest().getId())
                .remove();
    }
}