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
     * 提醒规则-指定门店活动提醒
     */
    public static final int STORE = 1;
    /**
     * 提醒规则-金额差小于指定金额
     */
    public static final int MAX_DIFF_PRICE = 2;
    /**
     * 提醒规则
     * 1：指定门店活动提醒
     * 2：金额差小于指定金额
     */
    private Integer type;
    /**
     * 描述
     */
    private String desc;
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
     * 上次通知时间
     */
    private Date lastNotifyTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 位置信息
     */
    private Location location;
    /**
     * 备注
     */
    private String remark;
    /**
     * 门店提醒扩展配置
     * 仅type为1时有值
     */
    private StoreExtNotifyConfig storeExtNotifyConfig;
    /**
     * 金额差提醒扩展配置
     * 仅type为2时有值
     */
    private MaxDiffPriceExtNotifyConfig maxDiffPriceExtNotifyConfig;

    public void initDesc(){
        switch (type){
            case STORE:
                desc = "门店：" + storeExtNotifyConfig.getStoreInfo().getName() ;
                break;
            case MAX_DIFF_PRICE:
                desc = "最小金额差：" + maxDiffPriceExtNotifyConfig.getMaxDiffPrice();
                break;
        }
    }

}
