package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaocan.model.dto.NotifyHistoryQueryDTO;
import io.github.xiaocan.model.entity.StorePushedHistoryEntity;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;

import java.util.List;

public interface StorePushedHistoryService extends IService<StorePushedHistoryEntity> {

    /**
     * 分页查询通知历史记录（当前用户）
     * @param dto 分页参数
     * @return 分页结果
     */
    Page<StorePushedHistoryVO> pageByUser(NotifyHistoryQueryDTO dto);


    StorePushedHistoryEntity findByNotifyIdAndUniqIdToday(Integer notifyId, String uniqId);

    StorePushedHistoryEntity findByNotifyIdAndUniqIdAll(Integer notifyId, String uniqId);

    /**
     * 批量保存并返回批次ID
     * @param entities 实体列表
     * @return 批次ID（UUID去-）
     */
    String saveBatchAndReturnBatchId(List<StorePushedHistoryEntity> entities);
}
