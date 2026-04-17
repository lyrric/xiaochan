package io.github.xiaochan.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaochan.model.enums.NotifyTypeEnums;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行记录
 * @author wangxiaodong
 * @date 2026/4/17
 */
@Data
@TableName("task_exec_history")
public class TaskExecHistoryEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer notifyConfigId;

    private NotifyTypeEnums notifyType;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 通知门店数量
     */
    private Integer notifyStoreCount;
    /**
     * 是否成功
     */
    private Boolean success;
    /**
     * 备注
     */
    private String remark;

}
