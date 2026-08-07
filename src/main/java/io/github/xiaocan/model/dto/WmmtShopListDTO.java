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
     * 位置ID，用于查询收藏状态
     */
    private Long locationId;

    /**
     * 分页游标数据，上一页接口返回的 scrollPageData，前端原样传回
     * 第一页传 null
     */
    private Object scrollPageData;

}
