package io.github.xiaocan.model.dto;

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




}
