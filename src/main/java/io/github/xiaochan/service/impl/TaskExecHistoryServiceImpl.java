package io.github.xiaochan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaochan.mapper.TaskExecHistoryMapper;
import io.github.xiaochan.model.dto.TaskExecHistoryEntity;
import io.github.xiaochan.service.TaskExecHistoryService;
import org.springframework.stereotype.Service;

@Service
public class TaskExecHistoryServiceImpl extends ServiceImpl<TaskExecHistoryMapper, TaskExecHistoryEntity> implements TaskExecHistoryService {
}
