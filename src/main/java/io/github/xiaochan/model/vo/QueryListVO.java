package io.github.xiaochan.model.vo;

import lombok.Data;

@Data
public class QueryListVO {

    /**
     * 门店名称，模糊搜索
     */
    private String name;
    /**
     * 排序类型，1：默认，2：返现金额排序 3：返现比例排序
     */
    private Integer orderType;
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
     * 只看可抢（剩余数量大于0）
     */
    private Boolean onlyAvailable;
}
