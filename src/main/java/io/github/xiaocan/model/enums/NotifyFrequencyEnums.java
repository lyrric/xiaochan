package io.github.xiaocan.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提醒频率
 */
@Getter
@AllArgsConstructor
public enum NotifyFrequencyEnums {
    ONCE("提醒一次"),
    DAILY("每日提醒"),
    NONE("不提醒");

    private final String description;
}
