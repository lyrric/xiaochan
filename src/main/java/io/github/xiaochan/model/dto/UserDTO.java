package io.github.xiaochan.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserDTO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String token;

    private String spt;

    /**
     * 验证码
     */
    private String sptCode;




}
