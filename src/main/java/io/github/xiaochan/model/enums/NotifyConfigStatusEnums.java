package io.github.xiaochan.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangxiaodong
 * @date 2026/4/17
 */
@Getter
@AllArgsConstructor
public enum NotifyConfigStatusEnums {
    ENABLE( "启用"),
    DISABLE( "停用"),

    ;
    private final String desc;
}
