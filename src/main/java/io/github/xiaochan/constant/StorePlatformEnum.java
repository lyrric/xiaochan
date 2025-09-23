package io.github.xiaochan.constant;

public enum StorePlatformEnum {
    /**
     * 未知
     */
    UNKNOWN(0, "未知"),
    /**
     * 美团
     */
    MEITUAN(1, "美团"),
    /**
     * 饿了么
     */
    ELEME(2, "饿了么"),
    /**
     * 京东
     */
    JD(3, "京东"),
    ;

    public final int type;
    public final String name;

    StorePlatformEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static StorePlatformEnum getByType(int type) {
        for (StorePlatformEnum value : values()) {
            if (value.type == type) {
                return value;
            }
        }
        return UNKNOWN;
    }
}