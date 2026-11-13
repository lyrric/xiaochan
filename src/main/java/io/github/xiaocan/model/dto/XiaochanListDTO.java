package io.github.xiaocan.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.xiaocan.constant.StorePlatformEnum;
import io.github.xiaocan.model.HttpProxyInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小蚕活动列表请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XiaochanListDTO {

    /** 门店名称，模糊搜索。 */
    private String name;

    /** 城市编码。 */
    @NotNull(message = "cityCode不能为空")
    private Integer cityCode;

    /** 经度。 */
    @NotNull(message = "longitude不能为空")
    private String longitude;

    /** 纬度。 */
    @NotNull(message = "latitude不能为空")
    private String latitude;

    /** 查询页码，从1开始。 */
    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum不能小于1")
    @Builder.Default
    private Integer pageNum = 1;

    /** 上一页的分页游标，第一页为空；兼容请求字段pvId。 */
    @JsonAlias("pvId")
    private String pagePvId;

    /** 返利类型：1满减，2比例返，默认满减。 */
    @Builder.Default
    private Integer rebateType = 1;

    /** 平台筛选：MEITUAN美团(1)、ELEME饿了么/淘宝闪购(2)、JD京东(3)，不传或空列表表示全部平台。 */
    private List<StorePlatformEnum> storePlatforms;

    /** HTTP代理信息，不传则直连。 */
    private HttpProxyInfo httpProxyInfo;

    /** 每页数量，默认20条。 */
    @NotNull(message = "pageSize不能为空")
    @Min(value = 10, message = "pageSize不能小于10")
    @Max(value = 30, message = "pageSize不能大于30")
    @Builder.Default
    private Integer pageSize = 20;

    /** 位置ID，用于查询收藏状态。 */
    private Long locationId;
}
