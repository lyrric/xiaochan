package io.github.xiaocan.model.dto;

import lombok.Data;

@Data
public class FavoriteStoreQueryDTO {

    private Long locationId;

    private String storeType;

    /**
     * 门店名称（模糊匹配）
     */
    private String storeName;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
