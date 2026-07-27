package io.github.xiaocan.model;

import io.github.xiaocan.model.enums.NotifyFrequencyEnums;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StoreExtNotifyConfig extends AbstractExtNotifyConfig{
    /**
     * 门店活动信息
     */
    private StoreInfo storeInfo;

    /**
     * 提醒频率
     */
    private NotifyFrequencyEnums remindFrequency = NotifyFrequencyEnums.ONCE;

}
