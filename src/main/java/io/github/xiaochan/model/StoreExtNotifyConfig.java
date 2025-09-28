package io.github.xiaochan.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StoreExtNotifyConfig extends AbstractExtNotifyConfig{
    /**
     * 是否只提醒一次
     */
    @NotNull
    private Boolean onlyOne;
    /**
     * 有效天数，为空则不限制天数
     */
    private Integer expireDay;
    /**
     * 活动信息
     */
    private StoreInfo storeInfo;

}
