package io.github.xiaocan.model.dto;

import io.github.xiaocan.model.enums.MonitorTypeEnums;
import lombok.Data;

/**
 * 通知历史记录分页查询请求参数
 */
@Data
public class NotifyHistoryQueryDTO {

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 监控ID
     */
    private Integer notifyConfigId;

    /**
     * 监控类型
     */
    private MonitorTypeEnums notifyType;
}
