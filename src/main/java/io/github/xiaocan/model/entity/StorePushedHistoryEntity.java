package io.github.xiaocan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 门店推送历史
 * @author lyrric
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("store_pushed_history")
public class StorePushedHistoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Integer notifyConfigId;

    private MonitorTypeEnums notifyType;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
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
     * 距离，单位米
     */
    private Integer distance;
    /**
     * 距离自带单位
     */
    private String distanceStr;
    /**
     * 满多少返
     */
    private BigDecimal price;
    /**
     * 返的金额
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




}
