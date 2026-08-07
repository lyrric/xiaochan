package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.mapper.StorePushedHistoryMapper;
import io.github.xiaocan.model.dto.NotifyHistoryQueryDTO;
import io.github.xiaocan.model.entity.StorePushedHistoryEntity;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;
import io.github.xiaocan.service.StorePushedHistoryService;
import io.github.xiaocan.service.UserService;
import io.github.xiaocan.service.FavoriteStoreService;
import io.github.xiaocan.utils.PageConvertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StorePushedHistoryServiceImpl extends ServiceImpl<StorePushedHistoryMapper, StorePushedHistoryEntity> implements StorePushedHistoryService {

    @Resource
    private UserService userService;
    @Resource
    private FavoriteStoreService favoriteStoreService;

    @Override
    public Page<StorePushedHistoryVO> pageByUser(NotifyHistoryQueryDTO dto) {
        // 获取当前用户ID
        Integer userId = userService.getByCurrentRequest().getId();

        // 使用lambdaQuery链式查询并转换为VO
        Page<StorePushedHistoryEntity> page = lambdaQuery()
                .eq(StorePushedHistoryEntity::getUserId, userId)
                .eq(dto.getNotifyConfigId() != null, StorePushedHistoryEntity::getNotifyConfigId, dto.getNotifyConfigId())
                .eq(dto.getNotifyType() != null, StorePushedHistoryEntity::getNotifyType, dto.getNotifyType())
                .orderByDesc(StorePushedHistoryEntity::getId)
                .page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        Page<StorePushedHistoryVO> voPage = PageConvertUtil.convert(page, StorePushedHistoryVO.class);
        favoriteStoreService.fillFavoriteIdsForPushedHistory(voPage.getRecords(), userId);
        return voPage;
    }

    @Override
    public StorePushedHistoryEntity findByNotifyIdAndUniqIdToday(Integer notifyId, String uniqId) {
        // 获取今天的开始时间和结束时间
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);

        return lambdaQuery()
                .eq(StorePushedHistoryEntity::getNotifyConfigId, notifyId)
                .eq(StorePushedHistoryEntity::getUniqId, uniqId)
                .last("limit 1")
                .between(StorePushedHistoryEntity::getCreateTime, todayStart, todayEnd)
                .one();
    }


    @Override
    public StorePushedHistoryEntity findByNotifyIdAndUniqIdAll(Integer notifyId, String uniqId) {
        return lambdaQuery()
                .eq(StorePushedHistoryEntity::getNotifyConfigId, notifyId)
                .eq(StorePushedHistoryEntity::getUniqId, uniqId)
                .last("limit 1")
                .one();
    }

    @Override
    public String saveBatchAndReturnBatchId(List<StorePushedHistoryEntity> entities) {
        // 生成UUID并去除-
        String batchId = UUID.randomUUID().toString().replace("-", "");
        // 为每个实体设置批次ID
        entities.forEach(entity -> entity.setBatchId(batchId));
        // 批量保存
        saveBatch(entities);
        return batchId;
    }
}
