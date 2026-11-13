package io.github.xiaocan.model.dto;

import io.github.xiaocan.model.HttpProxyInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** HTTP代理信息，不传则直连。 */
    private HttpProxyInfo httpProxyInfo;

    /** 每页数量，默认20条。 */
    @Builder.Default
    private int pageSize = 20;
}
