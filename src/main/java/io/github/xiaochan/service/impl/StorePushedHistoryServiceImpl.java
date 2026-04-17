package io.github.xiaochan.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaochan.mapper.StorePushedHistoryMapper;
import io.github.xiaochan.model.dto.NotifyHistoryQueryDTO;
import io.github.xiaochan.model.entity.StorePushedHistoryEntity;
import io.github.xiaochan.model.vo.StorePushedHistoryVO;
import io.github.xiaochan.service.StorePushedHistoryService;
import io.github.xiaochan.service.UserService;
import io.github.xiaochan.utils.PageConvertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StorePushedHistoryServiceImpl extends ServiceImpl<StorePushedHistoryMapper, StorePushedHistoryEntity> implements StorePushedHistoryService {

    @Resource
    private UserService userService;

    @Override
    public Page<StorePushedHistoryVO> pageByUser(NotifyHistoryQueryDTO dto) {
        // 获取当前用户ID
        Integer userId = userService.getByCurrentRequest().getId();

        // 使用lambdaQuery链式查询并转换为VO
        Page<StorePushedHistoryEntity> page = lambdaQuery()
                .eq(StorePushedHistoryEntity::getUserId, userId)
                .orderByDesc(StorePushedHistoryEntity::getCreateTime)
                .page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        return PageConvertUtil.convert(page, StorePushedHistoryVO.class);
    }

    @Override
    public StorePushedHistoryEntity findByNotifyIdAndStoreIdToday(Integer notifyId, Integer storeId) {
        // 获取今天的开始时间和结束时间
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);

        return lambdaQuery()
                .eq(StorePushedHistoryEntity::getNotifyConfigId, notifyId)
                .eq(StorePushedHistoryEntity::getStoreId, storeId)
                .last("limit 1")
                .between(StorePushedHistoryEntity::getCreateTime, todayStart, todayEnd)
                .one();
    }


    @Override
    public StorePushedHistoryEntity findByNotifyIdAndStoreIdAll(Integer notifyId, Integer storeId) {
        return lambdaQuery()
                .eq(StorePushedHistoryEntity::getNotifyConfigId, notifyId)
                .eq(StorePushedHistoryEntity::getStoreId, storeId)
                .last("limit 1")
                .one();
    }
}
