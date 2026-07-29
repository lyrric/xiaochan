package io.github.xiaocan.model.dto;

import io.github.xiaocan.model.enums.StoreTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用门店搜索请求参数
 * <p>
 * 聚合小蚕满减、小蚕美团赏金、歪麦三个平台的搜索结果
 */
@Data
public class StoreSearchDTO {

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
     * 来源门店类型
     * XC_MANJIAN 时不调用小蚕满减搜索，XC_MTSJ 时不调用小蚕美团赏金搜索
     * 同时用于与 uniqId 一起过滤掉来源门店自身
     */
    private StoreTypeEnum storeTypeEnum;

    /**
     * 来源门店唯一ID，与 storeTypeEnum 一起过滤掉来源门店自身
     */
    private String uniqId;

}
