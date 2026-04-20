package io.github.xiaocan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.xiaocan.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
