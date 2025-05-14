package io.github.xiaochan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    /**
     * 标识，如：公司
     */
    private String name;
    /**
     * 城市区编码
     */
    private Integer cityCode;
    /**
     * 纬度
     */
    public String latitude;
    /**
     * 经度
     */
    public String longitude;
    /**
     * 满返金额，大于等于此金额才可参与满返
     * 大于等于此值才通知
     * 为空不判断
     */
    public BigDecimal price;
    /**
     * 返现金额，大于此金额才通知
     * 为空不判断
     */
    private BigDecimal  rebatePrice;
    /**
     * 差值，price-rebatePrice大于等于此值才通知
     * 为空不判断
     */
    private BigDecimal difPrice;
    /**
     * 推送参数
     */
    private String spt;


}
