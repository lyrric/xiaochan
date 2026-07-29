package io.github.xiaocan.model;

import io.github.xiaocan.model.enums.StoreTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StoreInfo {

    /**
     * 门店名称
     */
    private String name;
    /**
     * 门店id
     */
    @Deprecated
    private Integer storeId;

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
     * 活动id
     * 同一个门店，这个活动id每天都是不一样的
     */
    private String promotionId;
    /**
     * 平台类型 1:美团，2：饿了么，3京东
     */
    private Integer type;
    /**
     * 活动开始时间 格式08:00
     */
    private String startTime;

    /**
     * 活动结束时间 格式21:00
     */
    private String endTime;
    /**
     * 剩余数量
     */
    private Integer leftNumber;

    /**
     * 距离,仅小蚕满减有值
     * 单位米
     */
    private Integer distance;

    /**
     * 距离自带单位
     */
    private String distanceStr;
    /**
     * 满多少返（仅满减）
     */
    private BigDecimal price;
    /**
     * 返的金额（仅满减）
     */
    private BigDecimal rebatePrice;
    /**
     * 返现百分比（仅美团赏金）
     */
    private BigDecimal rebateRatio;
    /**
     * 返现百分比-最高返金额（仅美团赏金）
     */
    private BigDecimal rebateMax;
    /**
     * 好评条件
     */
    private String rebateConditionStr;

    /**
     * 门店图片
     */
    private String icon;

    /**
     * 收藏记录ID（仅收藏门店模式有效）
     */
    private Long favoriteId;

    /**
     * 门店是否仍存在（仅收藏门店模式有效）
     */
    private Boolean exists;

    /**
     * 设置distance时，若distanceStr为空则同步生成distanceStr
     */
    public void setDistance(Integer distance) {
        this.distance = distance;
        if (distance != null && StringUtils.isBlank(this.distanceStr)) {
            if (distance >= 1000) {
                BigDecimal km = BigDecimal.valueOf(distance)
                        .divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP)
                        .stripTrailingZeros();
                this.distanceStr = km.toPlainString() + "km";
            } else {
                this.distanceStr = distance + "m";
            }
        }
    }

    /**
     * 设置distanceStr时，若distance为空则同步解析distance（单位：米）
     * distanceStr格式如 "500m"、"1.5KM"（大小写均可）
     */
    public void setDistanceStr(String distanceStr) {
        this.distanceStr = distanceStr;
        if (StringUtils.isNotBlank(distanceStr) && this.distance == null) {
            String lower = distanceStr.trim().toLowerCase();
            try {
                if (lower.endsWith("km")) {
                    String num = lower.substring(0, lower.length() - 2).trim();
                    this.distance = new BigDecimal(num)
                            .multiply(BigDecimal.valueOf(1000))
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                } else if (lower.endsWith("m")) {
                    String num = lower.substring(0, lower.length() - 1).trim();
                    this.distance = new BigDecimal(num)
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                }
            } catch (NumberFormatException ignored) {
                // 解析失败不设置distance
            }
        }
    }

}
