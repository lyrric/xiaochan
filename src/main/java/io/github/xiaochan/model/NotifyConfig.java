package io.github.xiaochan.model;

import lombok.Data;

import java.util.Date;

/**
 * 通知配置
 */
@Data
public class NotifyConfig {

    /**
     * id，实现方式为时间戳
     */
    private String id;
    /**
     * 提醒规则
     * 1：指定门店活动提醒
     * 2：自定义规则提醒
     */
    private Integer type;
    /**
     * 是否只提醒一次
     */
    private Boolean onlyOne;
    /**
     * 有效天数，为空则不限制天数
     */
    private Integer expireDay;
    /**
     * 状态
     * 0：停用，1：正常，2：已结束
     */
    private Integer status;
    /**
     * 通知次数
     */
    private Integer notifyCount;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 上次通知时间
     */
    private Date lastNotifyTime;
    /**
     * 位置信息
     */
    private Location location;
    /**
     * 活动信息
     */
    private StoreInfo storeInfo;
    /**
     * 备注
     */
    private String remark;
}
