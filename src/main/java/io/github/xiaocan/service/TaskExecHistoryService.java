package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaocan.model.dto.TaskExecHistoryQueryDTO;
import io.github.xiaocan.model.entity.TaskExecHistoryEntity;
import io.github.xiaocan.model.vo.TaskExecHistoryVO;

public interface TaskExecHistoryService extends IService<TaskExecHistoryEntity> {


    Page<TaskExecHistoryVO> pageByUser(TaskExecHistoryQueryDTO queryDTO);
}
