package io.github.xiaochan.service.impl;

import io.github.xiaochan.constant.RedisConstant;
import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.dto.NotifyConfigDTO;
import io.github.xiaochan.service.NotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 通知服务实现类
 *
 * @author xiaochan
 */
@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public String saveNotifyConfig(NotifyConfigDTO notifyConfigDTO) {
        log.info("保存通知配置: {}", notifyConfigDTO);
        // 转换DTO为实体
        NotifyConfig notifyConfig = new NotifyConfig();
        BeanUtils.copyProperties(notifyConfigDTO, notifyConfig);
        // 设置ID为当前时间戳
        notifyConfig.setId(String.valueOf(System.currentTimeMillis()));
        // 设置创建时间
        notifyConfig.setCreateTime(new Date());
        // 设置状态为正常
        notifyConfig.setStatus(1);
        // 初始化通知次数为0
        notifyConfig.setNotifyCount(0);
        notifyConfig.initDesc();
        getNotifyConfigMap().put(notifyConfig.getId(), notifyConfig);
        log.info("通知配置保存成功，配置ID: {}", notifyConfig.getId());
        return notifyConfig.getId();
    }

    @Override
    public void updateNotifyConfig(NotifyConfig notifyConfig) {
        getNotifyConfigMap().put(notifyConfig.getId(), notifyConfig);
    }

    private RMap<String, NotifyConfig> getNotifyConfigMap() {
        return redissonClient.getMap(RedisConstant.NOTIFY_CONFIG_MAP, new CompositeCodec(StringCodec.INSTANCE, JsonJacksonCodec.INSTANCE));
    }
    @Override
    public List<NotifyConfig> getNotifyConfigList() {
        List<NotifyConfig> notifyConfigs = new ArrayList<>(getNotifyConfigMap().values());
        return notifyConfigs.stream()
                .sorted(Comparator.comparing(NotifyConfig::getId)).toList();
    }

    @Override
    public boolean deleteNotifyConfig(String configId) {
        log.info("删除通知配置，配置ID: {}", configId);
        getNotifyConfigMap().remove(configId);
        return true;
    }
}