package io.github.xiaocan.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveFavoriteDTO {

    @NotNull(message = "locationId不能为空")
    private Long locationId;

    @NotBlank(message = "uniqueId不能为空")
    private String uniqueId;

    @NotBlank(message = "storeType不能为空")
    private String storeType;
}
