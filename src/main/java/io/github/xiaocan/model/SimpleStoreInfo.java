package io.github.xiaocan.model;

import io.github.xiaocan.model.enums.StoreTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleStoreInfo {

    /**
     * 门店名称
     */
    private String name;

    /**
     * 门店唯一id
     * storeId or wm_poi_id
     */
    private String uniqId;

    /**
     * 门店类型
     */
    private StoreTypeEnum storeTypeEnum;
    /**
     * 平台类型 1:美团，2：饿了么，3京东
     */
    private Integer type;
    /**
     * 距离
     * 单位米（小蚕满减）
     * 美团赏金自带单位
     */
    private String distance;
    /**
     * 门店图片
     */
    private String icon;


}
