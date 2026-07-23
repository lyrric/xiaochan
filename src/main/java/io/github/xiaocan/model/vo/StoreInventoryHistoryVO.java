package io.github.xiaocan.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门店库存记录返回VO
 * @author wangxiaodong
 * @date 2026/7/23
 */
@Data
@NoArgsConstructor
public class StoreInventoryHistoryVO {

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
     * 创建时间
     */
    @JsonFormat(pattern = "dd HH:mm")
    private LocalDateTime createTime;

}
