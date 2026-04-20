package io.github.xiaocan.model;

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

}
