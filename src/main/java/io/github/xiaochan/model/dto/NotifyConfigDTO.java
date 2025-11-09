package io.github.xiaochan.model.dto;

import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.MaxDiffPriceExtNotifyConfig;
import io.github.xiaochan.model.StoreExtNotifyConfig;
import jakarta.validation.Valid;
import lombok.Data;

/**
 * 通知配置
 */
@Data
public class NotifyConfigDTO {
    /**
     * id，实现方式为时间戳
     * 由后端生成
     */
    private String id;
    /**
     * 提醒规则
     * 1：指定门店活动提醒
     * 2：金额差小于指定金额
     */
    private Integer type;
    /**
     * 位置信息
     */
    private Location location;
    /**
     * 门店提醒扩展配置
     */
    @Valid
    private StoreExtNotifyConfig storeExtNotifyConfig;
    /**
     * 金额差提醒扩展配置
     */
    @Valid
    private MaxDiffPriceExtNotifyConfig maxDiffPriceExtNotifyConfig;

}
