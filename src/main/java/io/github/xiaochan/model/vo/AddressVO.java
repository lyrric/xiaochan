package io.github.xiaochan.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressVO {

    /**
     * id
     */
    private String id;
    /**
     * 标题 例如：四川大学(望江校区)
     */
    private String title;
    /**
     * 地址：四川省成都市武侯区一环路南一段24号
     */
    private String address;
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 经度
     */
    private String longitude;
    /**
     * cityCode
     */
    private String cityCode;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 区
     */
    private String district;
}
