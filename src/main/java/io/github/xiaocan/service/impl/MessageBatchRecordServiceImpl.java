package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.mapper.MessageBatchRecordMapper;
import io.github.xiaocan.model.entity.MessageBatchRecordEntity;
import io.github.xiaocan.model.entity.StorePushedHistoryEntity;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;
import io.github.xiaocan.service.MessageBatchRecordService;
import io.github.xiaocan.service.StorePushedHistoryService;
import io.github.xiaocan.service.UserService;
import io.github.xiaocan.service.FavoriteStoreService;
import io.github.xiaocan.utils.PageConvertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MessageBatchRecordServiceImpl extends ServiceImpl<MessageBatchRecordMapper, MessageBatchRecordEntity> implements MessageBatchRecordService {

    @Resource
    private UserService userService;
    @Resource
    private StorePushedHistoryService storePushedHistoryService;
    @Resource
    private FavoriteStoreService favoriteStoreService;

    @Override
    public Long recordBatch(Integer userId, String batchIds) {
        MessageBatchRecordEntity entity = new MessageBatchRecordEntity();
        entity.setUserId(userId);
        entity.setBatchIds(batchIds);
        entity.setCreateTime(LocalDateTime.now());
        save(entity);
        return entity.getId();
    }

    @Override
    public List<StorePushedHistoryVO> getPushedHistoryByRecordId(Long id) {
        // 1. 查询消息批次记录
        MessageBatchRecordEntity record = getById(id);
        if (record == null) {
            throw new BusinessException("消息批次记录不存在");
        }

        // 2. 校验是否是当前用户
        Integer currentUserId = userService.getByCurrentRequest().getId();
        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权访问该记录");
        }

        // 3. 解析batchIds
        String batchIds = record.getBatchIds();
        if (batchIds == null || batchIds.isBlank()) {
            return Collections.emptyList();
        }
        List<String> batchIdList = Arrays.asList(batchIds.split(","));

        // 4. 根据batchIds查询门店推送历史
        List<StorePushedHistoryEntity> entities = storePushedHistoryService.lambdaQuery()
                .in(StorePushedHistoryEntity::getBatchId, batchIdList)
                .orderByDesc(StorePushedHistoryEntity::getId)
                .list();

        // 5. 转换为VO
        List<StorePushedHistoryVO> voList = PageConvertUtil.convertList(entities, StorePushedHistoryVO.class);
        // 6. 填充收藏ID
        favoriteStoreService.fillFavoriteIdsForPushedHistory(voList, currentUserId);
        return voList;
    }
}
