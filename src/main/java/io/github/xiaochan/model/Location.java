package io.github.xiaochan.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     * id
     */
    private String id;

    /**
     * 标识，如：公司
     */
    @NotBlank(message = "地址名称不能为空")
    private String name;
    /**
     *  地址
     */
    @NotBlank(message = "地址不能为空")
    private String address;
    /**
     * 城市区编码
     */
    @NotNull(message = "城市编码不能为空")
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
     * 推送参数
     */
    private String spt;
    /**
     * 是否推送
     */
    @NotNull(message = "是否推送不能为空")
    private Boolean pushSwitch;


}
