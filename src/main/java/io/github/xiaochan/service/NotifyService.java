package io.github.xiaochan.service;

import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.dto.NotifyConfigDTO;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author xiaochan
 */
public interface NotifyService {

    /**
     * 保存通知配置
     *
     * @param notifyConfigDTO 通知配置DTO
     * @return 配置ID
     */
    String saveNotifyConfig(NotifyConfigDTO notifyConfigDTO);

    /**
     * 获取所有通知配置
     *
     * @return 通知配置列表
     */
    List<NotifyConfig> getNotifyConfigList();

    /**
     * 删除通知配置
     *
     * @param configId 配置ID
     * @return 是否删除成功
     */
    boolean deleteNotifyConfig(String configId);

    /**
     * 修改配置
     * @param notifyConfig
     */
    void updateNotifyConfig(NotifyConfig notifyConfig);
}