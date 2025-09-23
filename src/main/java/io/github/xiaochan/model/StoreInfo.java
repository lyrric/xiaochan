package io.github.xiaochan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StoreInfo {

    /**
     * 门店名称
     */
    private String name;
    /**
     * 门店id
     */
    private Integer storeId;
    /**
     * 是否是新店
     */
    private Boolean ifNew;
    /**
     * 营业时间 10:00-22:00
     */
    private String openHours;
    /**
     * 活动id
     * 同一个门店，这个活动id每天都是不一样的
     */
    private Integer promotionId;
    /**
     * 平台类型 1:美团，2：饿了么，3京东
     */
    private Integer type;
    /**
     * 活动开始时间 格式08:00
     */
    private String startTime;

    /**
     * 活动结束时间 格式21:00
     */
    private String endTime;
    /**
     * 剩余数量
     */
    private Integer leftNumber;

    /**
     * 距离，单位米
     */
    private Integer distance;
    /**
     * 满多少返
     */
    private BigDecimal price;
    /**
     * 返的金额
     */
    private BigDecimal rebatePrice;
    /**
     * 好评条件
     * 99：无需评价
     * 2：图文评价
     */
    private Integer rebateCondition;
    /**
     * 门店图片
     */
    private String icon;

}
