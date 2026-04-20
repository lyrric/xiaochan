package io.github.xiaocan.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangxiaodong
 * @date 2026/4/15
 */
@AllArgsConstructor
@Getter
public enum MonitorTypeEnums {

    STORE_ACTIVITY( "指定门店"),
    MINIMUM_PAY( "最小实付");

    private final String description;

}
