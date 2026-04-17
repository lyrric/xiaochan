package io.github.xiaochan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaochan.model.dto.NotifyHistoryQueryDTO;
import io.github.xiaochan.model.entity.StorePushedHistoryEntity;
import io.github.xiaochan.model.vo.StorePushedHistoryVO;

public interface StorePushedHistoryService extends IService<StorePushedHistoryEntity> {

    /**
     * 分页查询通知历史记录（当前用户）
     * @param dto 分页参数
     * @return 分页结果
     */
    Page<StorePushedHistoryVO> pageByUser(NotifyHistoryQueryDTO dto);


    StorePushedHistoryEntity findByNotifyIdAndStoreIdToday(Integer notifyId, Integer storeId);

    StorePushedHistoryEntity findByNotifyIdAndStoreIdAll(Integer notifyId, Integer storeId);
}
