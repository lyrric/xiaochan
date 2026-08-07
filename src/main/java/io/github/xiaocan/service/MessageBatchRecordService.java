package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaocan.model.entity.MessageBatchRecordEntity;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;

import java.util.List;

public interface MessageBatchRecordService extends IService<MessageBatchRecordEntity> {

    /**
     * 记录消息批次
     * @param userId 用户ID
     * @param batchIds 批次ID（多个以逗号分割，已去重）
     * @return 插入记录的ID
     */
    Long recordBatch(Integer userId, String batchIds);

    /**
     * 根据消息批次记录ID查询关联的门店推送历史
     * @param id 消息批次记录ID
     * @return 门店推送历史列表
     */
    List<StorePushedHistoryVO> getPushedHistoryByRecordId(Long id);
}
