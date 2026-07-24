package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.mapper.StoreInventoryHistoryMapper;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.StoreInventoryHistoryEntity;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.StoreInventoryHistoryVO;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
                    entity.setSkuId(storeInfo.getPromotionId() != null ? String.valueOf(storeInfo.getPromotionId()) : "");
                    entity.setSkuName(buildSkuName(storeInfo));
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
        // 根据创建时间正序排列后，按 skuId 分组，对每组内连续相同 inventory 的记录去重：
        // 每个连续区间只保留第一条和最后一条（区间长度为 1 时保留该条）
        Map<String, List<StoreInventoryHistoryEntity>> grouped = entities.stream()
                .collect(Collectors.groupingBy(e -> Objects.toString(e.getSkuId(), ""), LinkedHashMap::new, Collectors.toList()));

        List<StoreInventoryHistoryEntity> deduped = new ArrayList<>();
        for (List<StoreInventoryHistoryEntity> group : grouped.values()) {
            int size = group.size();
            for (int i = 0; i < size; i++) {
                StoreInventoryHistoryEntity current = group.get(i);
                boolean isFirstOfRun = i == 0
                        || !Objects.equals(current.getInventory(), group.get(i - 1).getInventory());
                boolean isLastOfRun = i == size - 1
                        || !Objects.equals(current.getInventory(), group.get(i + 1).getInventory());
                if (isFirstOfRun || isLastOfRun) {
                    deduped.add(current);
                }
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

    private String buildSkuName(StoreInfo storeInfo) {
        if (Objects.equals(storeInfo.getStoreTypeEnum(), StoreTypeEnum.XC_MANJIAN)) {
            return "满" + stripTrailingZeros(storeInfo.getPrice()) + "返" + stripTrailingZeros(storeInfo.getRebatePrice());
        }
        if (Objects.equals(storeInfo.getStoreTypeEnum(), StoreTypeEnum.XC_MTSJ)) {
            return "返" + stripTrailingZeros(storeInfo.getRebateRatio()) + "%最高" + stripTrailingZeros(storeInfo.getRebateMax());
        }
        return "未知";
    }

    private String stripTrailingZeros(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }
}
