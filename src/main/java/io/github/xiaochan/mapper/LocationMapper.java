package io.github.xiaochan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.xiaochan.model.entity.LocationEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocationMapper extends BaseMapper<LocationEntity> {
}
