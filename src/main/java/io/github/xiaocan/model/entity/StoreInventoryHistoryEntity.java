package io.github.xiaocan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门店库存记录
 * @author wangxiaodong
 * @date 2026/7/23
 */
@Data
@TableName("store_inventory_history")
public class StoreInventoryHistoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 门店名称
     */
    private String name;

    /**
     * 门店唯一标识
     */
    private String uniqueId;

    /**
     * 库存数量
     */
    private Integer inventory;

    /**
     * 门店类型
     */
    private StoreTypeEnum storeType;

    /**
     * 活动id（对应 StoreInfo.promotionId）
     */
    private String skuId;

    /**
     * 活动名称（如：满X返Y、返x%最高Y）
     */
    private String skuName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}