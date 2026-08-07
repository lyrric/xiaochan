package io.github.xiaocan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息批次记录
 * 记录每次发送消息时关联的推送批次ID
 */
@Data
@TableName("message_batch_record")
public class MessageBatchRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 批次ID（多个以逗号分割，已去重）
     */
    private String batchIds;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
