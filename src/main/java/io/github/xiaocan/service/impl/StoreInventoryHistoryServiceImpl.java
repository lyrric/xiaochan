package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.mapper.StoreInventoryHistoryMapper;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.StoreInventoryHistoryEntity;
import io.github.xiaocan.model.vo.StoreInventoryHistoryVO;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class StoreInventoryHistoryServiceImpl extends ServiceImpl<StoreInventoryHistoryMapper, StoreInventoryHistoryEntity> implements StoreInventoryHistoryService {


    @Override
    public void insertBatch(List<StoreInfo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<StoreInventoryHistoryEntity> entities = list.stream()
                .filter(storeInfo -> Objects.nonNull(storeInfo.getLeftNumber()))
                .map(storeInfo -> {
                    StoreInventoryHistoryEntity entity = new StoreInventoryHistoryEntity();
                    entity.setName(storeInfo.getName());
                    entity.setUniqueId(storeInfo.getUniqId());
                    entity.setInventory(storeInfo.getLeftNumber());
                    entity.setStoreType(storeInfo.getStoreTypeEnum());
                    entity.setCreateTime(now);
                    return entity;
                })
                .toList();
        if (entities.isEmpty()) {
            return;
        }
        saveBatch(entities);
    }

    @Override
    public List<StoreInventoryHistoryVO> listTodayByUniqueId(String uniqueId) {
        // 获取今天的开始时间和结束时间
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<StoreInventoryHistoryEntity> entities = lambdaQuery()
                .eq(StoreInventoryHistoryEntity::getUniqueId, uniqueId)
                .between(StoreInventoryHistoryEntity::getCreateTime, todayStart, todayEnd)
                .orderByAsc(StoreInventoryHistoryEntity::getCreateTime)
                .orderByAsc(StoreInventoryHistoryEntity::getId)
                .list();
        // 根据创建时间正序排列后，对连续相同 inventory 的记录去重：
        // 每个连续区间只保留第一条和最后一条（区间长度为 1 时保留该条）
        List<StoreInventoryHistoryEntity> deduped = new ArrayList<>();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            StoreInventoryHistoryEntity current = entities.get(i);
            boolean isFirstOfRun = i == 0
                    || !Objects.equals(current.getInventory(), entities.get(i - 1).getInventory());
            boolean isLastOfRun = i == size - 1
                    || !Objects.equals(current.getInventory(), entities.get(i + 1).getInventory());
            if (isFirstOfRun || isLastOfRun) {
                deduped.add(current);
            }
        }
        return deduped.stream()
                .map(entity -> {
                    StoreInventoryHistoryVO vo = new StoreInventoryHistoryVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .toList();
    }
}
