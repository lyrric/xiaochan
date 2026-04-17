package io.github.xiaochan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.xiaochan.model.dto.TaskExecHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskExecHistoryMapper extends BaseMapper<TaskExecHistoryEntity> {
}
