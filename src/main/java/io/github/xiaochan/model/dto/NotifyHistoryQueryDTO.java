package io.github.xiaochan.model.dto;

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
}
