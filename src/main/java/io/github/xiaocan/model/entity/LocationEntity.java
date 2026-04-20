package io.github.xiaocan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("location")
public class LocationEntity {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    /**
     * 标识，如：公司
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 城市区编码
     */
    private Integer cityCode;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    @TableLogic
    private Boolean deleted;

    private LocalDateTime createTime;

}
