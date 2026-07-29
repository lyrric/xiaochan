package io.github.xiaocan.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 歪麦-门店列表请求参数
 */
@Data
public class WmmtShopListDTO {

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
     * 翻页游标，上一页返回的pagePvId
     * 第一页为空
     */
    private String pvId;

}
