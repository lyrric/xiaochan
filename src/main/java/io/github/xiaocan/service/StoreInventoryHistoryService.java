package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.StoreInventoryHistoryEntity;
import io.github.xiaocan.model.vo.StoreInventoryHistoryVO;

import java.util.List;

public interface StoreInventoryHistoryService extends IService<StoreInventoryHistoryEntity> {


    void insertBatch(List<StoreInfo> list);

    /**
     * 查询当前用户当天指定门店的库存记录
     * @param uniqueId 门店唯一标识
     * @return 当天所有库存记录
     */
    List<StoreInventoryHistoryVO> listTodayByUniqueId(String uniqueId);
}