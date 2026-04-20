package io.github.xiaocan.model.vo;

import io.github.xiaocan.model.MinimumPayExtNotifyConfig;
import io.github.xiaocan.model.StoreExtNotifyConfig;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import lombok.Data;

/**
 * 通知配置
 */
@Data
public class NotifyConfigVO {
    /**
     * id，实现方式为时间戳
     * 由后端生成
     */
    private Integer id;
    /**
     * 提醒规则
     */
    private MonitorTypeEnums type;
    /**
     * 位置信息
     */
    private Long locationId;
    /**
     * 运行时间
     */
    private Integer startHour;
    /**
     * 结束时间
     */
    private Integer endHour;
    /**
     * 运行星期内配置,从1开始，多个以,分隔
     */
    private String weeks;
    /**
     * 门店提醒扩展配置
     */
    private StoreExtNotifyConfig storeExtNotifyConfig;
    /**
     * 金额差提醒扩展配置
     */
    private MinimumPayExtNotifyConfig minimumPayExtNotifyConfig;

}
