package io.github.xiaocan.model.dto;

import io.github.xiaocan.constant.StorePlatformEnum;
import io.github.xiaocan.model.HttpProxyInfo;
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

    /** 城市编码。 */
    private Integer cityCode;

    /** 经度。 */
    private String longitude;

    /** 纬度。 */
    private String latitude;

    /** 分页偏移量，默认从0开始。 */
    private int offset;

    /** 分页游标。 */
    private String pagePvId;

    /** 返利类型：1满减，2比例返，默认满减。 */
    @Builder.Default
    private Integer rebateType = 1;

    /** 平台筛选：MEITUAN美团(1)、ELEME饿了么/淘宝闪购(2)、JD京东(3)，不传或空列表表示全部平台。 */
    private List<StorePlatformEnum> storePlatforms;

    /** HTTP代理信息，不传则直连。 */
    private HttpProxyInfo httpProxyInfo;

    /** 每页数量，默认20条。 */
    @Builder.Default
    private int pageSize = 20;
}
