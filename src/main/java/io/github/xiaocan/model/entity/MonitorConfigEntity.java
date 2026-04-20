package io.github.xiaocan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *监控配置
 */
@Data
@TableName("monitor_config")
public class MonitorConfigEntity {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 提醒规则
     */
    private MonitorTypeEnums type;
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
    private MonitorConfigStatusEnums status;
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
