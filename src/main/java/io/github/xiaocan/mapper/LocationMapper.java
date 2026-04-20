package io.github.xiaocan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.xiaocan.model.entity.LocationEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocationMapper extends BaseMapper<LocationEntity> {
}
