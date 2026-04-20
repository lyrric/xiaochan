package io.github.xiaocan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotifyConfigMapper extends BaseMapper<MonitorConfigEntity> {
}
