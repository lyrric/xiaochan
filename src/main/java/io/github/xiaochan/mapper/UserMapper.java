package io.github.xiaochan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.xiaochan.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
