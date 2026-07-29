package io.github.xiaocan.model.dto;

import io.github.xiaocan.model.enums.StoreTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FavoriteStoreListDTO {

    @NotNull(message = "locationId不能为空")
    private Long locationId;

    /**
     * 门店类型列表，为空时查询全部类型
     */
    private List<StoreTypeEnum> storeTypes;
}
