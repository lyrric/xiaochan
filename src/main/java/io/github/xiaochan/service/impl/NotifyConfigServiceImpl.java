package io.github.xiaochan.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaochan.config.BusinessException;
import io.github.xiaochan.mapper.NotifyConfigMapper;
import io.github.xiaochan.model.dto.NotifyConfigDTO;
import io.github.xiaochan.model.entity.NotifyConfigEntity;
import io.github.xiaochan.model.entity.UserEntity;
import io.github.xiaochan.model.enums.NotifyConfigStatusEnums;
import io.github.xiaochan.model.enums.NotifyTypeEnums;
import io.github.xiaochan.model.vo.NotifyConfigVO;
import io.github.xiaochan.service.NotifyConfigService;
import io.github.xiaochan.service.UserService;
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
public class NotifyConfigServiceImpl extends ServiceImpl<NotifyConfigMapper, NotifyConfigEntity> implements NotifyConfigService {

    @Resource
    private UserService userService;

    @Override
    public List<NotifyConfigVO> listByUserId() {
        return this.lambdaQuery().eq(NotifyConfigEntity::getUserId, userService.getByCurrentRequest().getId()).list().stream().map(entity->{
            NotifyConfigVO vo = new NotifyConfigVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<NotifyConfigEntity> list(NotifyTypeEnums type, NotifyConfigStatusEnums enums) {
        return this.lambdaQuery().eq(NotifyConfigEntity::getType, type).eq(NotifyConfigEntity::getStatus, enums).list();
    }

    @Override
    public void addUpdateConfig(NotifyConfigDTO dto) {
        log.info("保存通知配置请求: {}", dto);
        UserEntity user = userService.getByCurrentRequest();
        NotifyConfigEntity entity;
        if (dto.getId() != null) {
            entity = getById(dto.getId());
            if (entity == null || !entity.getUserId().equals(user.getId())) {
                throw new BusinessException("无权修改该通知配置");
            }
        }else{
            entity = new NotifyConfigEntity();
            entity.setUserId(user.getId());
        }
        BeanUtils.copyProperties(dto, entity);
        if (dto.getType() == NotifyTypeEnums.STORE_ACTIVITY) {
            entity.setExtConfig(JSONObject.toJSONString(dto.getStoreExtNotifyConfig()));
        }else{
            entity.setExtConfig(JSONObject.toJSONString(dto.getMinimumPayExtNotifyConfig()));
        }
        saveOrUpdate(entity);
    }

    @Override
    public void updateConfig(int id, NotifyConfigStatusEnums statusEnums, String remark) {
        this.lambdaUpdate().eq(NotifyConfigEntity::getId, id)
                .set(NotifyConfigEntity::getStatus, statusEnums)
                .set(NotifyConfigEntity::getRemark, remark)
                .update();
    }

    @Override
    public void deleteById(Integer configId) {
        this.lambdaUpdate().eq(NotifyConfigEntity::getId, configId)
                .eq(NotifyConfigEntity::getUserId, userService.getByCurrentRequest().getId())
                .remove();
    }
}