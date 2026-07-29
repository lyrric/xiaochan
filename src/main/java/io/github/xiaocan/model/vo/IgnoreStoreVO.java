package io.github.xiaocan.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IgnoreStoreVO {

    /**
     * 门店id
     */
    private String storeId;
    /**
     * 名单名称
     */
    private String storeName;
    /**
     * 平台类型 1:美团，2：饿了么，3京东
     */
    private Integer type;
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
     */
    private String rebateConditionStr;
    /**
     * 门店图片
     */
    private String icon;
}
