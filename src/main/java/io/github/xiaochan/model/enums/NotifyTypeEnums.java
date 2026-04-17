package io.github.xiaochan.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangxiaodong
 * @date 2026/4/15
 */
@AllArgsConstructor
@Getter
public enum NotifyTypeEnums {

    STORE_ACTIVITY( "指定门店"),
    MINIMUM_PAY( "最小实付");

    private final String description;

}
