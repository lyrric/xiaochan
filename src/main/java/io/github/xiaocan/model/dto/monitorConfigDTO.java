package io.github.xiaocan.model.dto;

import io.github.xiaocan.model.MinimumPayExtNotifyConfig;
import io.github.xiaocan.model.StoreExtNotifyConfig;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知配置
 */
@Data
public class monitorConfigDTO {
    /**
     * id
     */
    private Integer id;
    /**
     * 提醒规则
     */
    @NotNull
    private MonitorTypeEnums type;
    /**
     * 位置信息
     */
    @NotNull
    private Long locationId;
    /**
     * 运行时间
     */
    @NotNull
    private Integer startHour;
    /**
     * 结束时间
     */
    @NotNull
    private Integer endHour;
    /**
     * 运行星期内配置,从1开始，多个以,分隔
     */
    @NotEmpty
    private String weeks;
    /**
     * 门店提醒扩展配置
     */
    @Valid
    private StoreExtNotifyConfig storeExtNotifyConfig;
    /**
     * 金额差提醒扩展配置
     */
    @Valid
    private MinimumPayExtNotifyConfig minimumPayExtNotifyConfig;

}
