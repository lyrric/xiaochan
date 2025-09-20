package io.github.xiaochan.model.vo;

import lombok.Data;

@Data
public class BookVO {

    /**
     * 活动id
     */
    private Integer promotionId;
    /**
     * 活动开始时间 格式08:00
     */
    private String startTime;


}
