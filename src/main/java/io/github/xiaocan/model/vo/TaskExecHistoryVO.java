package io.github.xiaocan.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行记录
 * @author wangxiaodong
 * @date 2026/4/17
 */
@Data
@TableName("task_exec_history")
public class TaskExecHistoryVO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer notifyConfigId;

    private MonitorTypeEnums notifyType;
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
