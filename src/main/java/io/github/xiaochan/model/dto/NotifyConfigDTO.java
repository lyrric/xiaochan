package io.github.xiaochan.model.dto;

import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知配置
 */
@Data
public class NotifyConfigDTO {
    /**
     * id 时间戳
     */
    private String id;
    /**
     * 提醒规则
     * 1：指定门店活动提醒
     * 2：自定义规则提醒
     */
    @NotNull(message = "提醒规则不能为空")
    private Integer type;
    /**
     * 是否只提醒一次
     */
    @NotNull(message = "onlyOne不能为空")
    private Boolean onlyOne;
    /**
     * 有效天数，为空则不限制天数
     */
    @Min(value = 1,message = "有效天数不能为0")
    private Integer expireDay;
    /**
     * 位置信息
     */
    @NotNull(message = "位置信息不能为空")
    private Location location;
    /**
     * 活动信息
     */
    @NotNull(message = "活动信息不能为空")
    private StoreInfo storeInfo;
}
