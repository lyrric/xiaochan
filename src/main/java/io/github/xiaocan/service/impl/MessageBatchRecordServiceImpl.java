package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.mapper.MessageBatchRecordMapper;
import io.github.xiaocan.model.entity.MessageBatchRecordEntity;
import io.github.xiaocan.service.MessageBatchRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MessageBatchRecordServiceImpl extends ServiceImpl<MessageBatchRecordMapper, MessageBatchRecordEntity> implements MessageBatchRecordService {

    @Override
    public Long recordBatch(Integer userId, String batchIds) {
        MessageBatchRecordEntity entity = new MessageBatchRecordEntity();
        entity.setUserId(userId);
        entity.setBatchIds(batchIds);
        entity.setCreateTime(LocalDateTime.now());
        save(entity);
        return entity.getId();
    }
}
