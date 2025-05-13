package io.github.xiaochan.enums;

public enum LocationEnum {

    /**
     * 在此填入经纬度
     */
    WXD_WORK("1","2");
    /**
     * 纬度
     */
    public final String latitude;
    /**
     * 经度
     */
    public final String longitude;

    LocationEnum(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


}
