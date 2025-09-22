package io.github.xiaochan.model.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "cityCode不能为空")
    private Integer cityCode;
    /**
     * 纬度
     */
    @NotNull(message = "latitude不能为空")
    public String latitude;
    /**
     * 经度
     */
    @NotNull(message = "longitude不能为空")
    public String longitude;
    /**
     * 只看可抢（剩余数量大于0）
     */
    private Boolean onlyAvailable;
    /**
     * 页码
     */
    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum不能小于1")
    private Integer pageNum = 1;
    /**
     * 每页数量
     */
    @NotNull(message = "pageSize不能为空")
    @Max(value = 30, message = "pageSize不能大于30")
    @Min(value = 10, message = "pageSize不能小于10")
    private Integer pageSize = 30;

}
