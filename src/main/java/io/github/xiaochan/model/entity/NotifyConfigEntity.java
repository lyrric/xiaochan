package io.github.xiaochan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaochan.model.enums.NotifyConfigStatusEnums;
import io.github.xiaochan.model.enums.NotifyTypeEnums;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知配置
 */
@Data
@TableName("notify_config")
public class NotifyConfigEntity {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 提醒规则
     */
    private NotifyTypeEnums type;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 位置信息
     */
    private Long locationId;
    /**
     * 运行时间
     */
    @NotNull
    private Integer startHour;
    /**
     * 结束时间
     */
    @NotNull
    private Integer endHour;
    /**
     * 运行星期内配置,从1开始，多个以,分隔
     */
    @NotEmpty
    private String weeks;
    /**
     * 门店提醒扩展配置
     */
    private String extConfig;
    /**
     * 状态
     */
    private NotifyConfigStatusEnums status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;


}
