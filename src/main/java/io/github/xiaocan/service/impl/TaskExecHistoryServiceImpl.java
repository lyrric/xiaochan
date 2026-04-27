package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.mapper.TaskExecHistoryMapper;
import io.github.xiaocan.model.dto.TaskExecHistoryQueryDTO;
import io.github.xiaocan.model.entity.TaskExecHistoryEntity;
import io.github.xiaocan.model.vo.TaskExecHistoryVO;
import io.github.xiaocan.service.TaskExecHistoryService;
import io.github.xiaocan.service.UserService;
import io.github.xiaocan.utils.PageConvertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TaskExecHistoryServiceImpl extends ServiceImpl<TaskExecHistoryMapper, TaskExecHistoryEntity> implements TaskExecHistoryService {

    @Resource
    private UserService userService;

    @Override
    public Page<TaskExecHistoryVO> pageByUser(TaskExecHistoryQueryDTO queryDTO) {
        Page<TaskExecHistoryEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        this.lambdaQuery()
                .eq(TaskExecHistoryEntity::getUserId, userService.getByCurrentRequest().getId())
                .eq(queryDTO.getNotifyConfigId() != null, TaskExecHistoryEntity::getNotifyConfigId, queryDTO.getNotifyConfigId())
                .orderByDesc(TaskExecHistoryEntity::getId)
                .page(page);
        return PageConvertUtil.convert(page, TaskExecHistoryVO.class);
    }
}
