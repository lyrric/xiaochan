package io.github.xiaocan.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangxiaodong
 * @date 2026/7/15
 */
@AllArgsConstructor
@Getter
public enum StoreTypeEnum {

    /**
     * 小蚕满减
     */
    XC_MANJIAN("小蚕满减"),
    /**
     * 小蚕比例返
     */
    XC_MTSJ("小蚕比例返"),

    /**
     * 歪卖-满减
     */
    WM_MANJIAN("歪卖满减"),

    /**
     * 歪卖-比例返
     */
    WM_MTSJ("歪卖比例返"),
    ;

    private final String description;
}
