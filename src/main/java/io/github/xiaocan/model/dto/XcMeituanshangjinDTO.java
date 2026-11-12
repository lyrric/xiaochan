package io.github.xiaocan.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小蚕-美团赏金
 */
@Data
public class XcMeituanshangjinDTO {

    /**
     * 门店名称，模糊搜索
     */
    private String name;

    /**
     * 城市区编码
     */
    @NotNull(message = "cityCode不能为空")
    private Integer cityCode;
    /**
     * 纬度
     */
    @NotNull(message = "latitude不能为空")
    private String latitude;
    /**
     * 经度
     */
    @NotNull(message = "longitude不能为空")
    private String longitude;

    /**
     * pvId上一页的pvId
     * 第一页为空
     */
    private String pvId;

    /**
     * 页码，从1开始
     */
    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum不能小于1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @NotNull(message = "pageSize不能为空")
    @Min(value = 10, message = "pageSize不能小于10")
    @Max(value = 30, message = "pageSize不能大于30")
    private Integer pageSize = 20;

    /**
     * 位置ID，用于查询收藏状态
     */
    private Long locationId;




}
