package io.github.xiaocan.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite_store")
public class FavoriteStoreEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Long locationId;

    private String uniqId;

    private StoreTypeEnum storeType;

    private String icon;

    private String name;

    private Integer type;

    private String distance;

    private LocalDateTime createTime;

    @TableLogic
    private Boolean deleted;
}
