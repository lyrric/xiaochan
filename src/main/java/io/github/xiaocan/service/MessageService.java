package io.github.xiaocan.service;

import io.github.xiaocan.constant.StorePlatformEnum;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息发送服务
 * <p>
 * 将消息暂存缓冲区，延迟1分钟后按 spt + MonitorTypeEnums 分组合并发送。
 * 同一分组内多个消息会合并为一条消息，消息体中包含地址信息。
 *
 * @author wangxiaodong
 */
@Slf4j
@Service
public class MessageService {

    @Resource
    private UserService userService;
    @Resource
    private SptService sptService;

    /**
     * 延迟发送时间（秒）
     */
    private static final long DELAY_SECONDS = 60;

    /**
     * 消息缓冲区，key = spt + ":" + monitorType
     */
    private final ConcurrentHashMap<String, MessageBatch> pendingBatches = new ConcurrentHashMap<>();

    /**
     * 延迟调度器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "message-flush");
        t.setDaemon(true);
        return t;
    });

    /**
     * 默认 body 卡片模板（内联 CSS，兼容微信推送 HTML 渲染）
     */
    private static final String DEFAULT_BODY_TEMPLATE =
            "<div style=\"background:#f7f8fa;border-radius:12px;padding:16px;margin:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;\">" +
            "  <div style=\"display:flex;align-items:center;margin-bottom:12px;\">" +
            "    <span style=\"font-size:15px;font-weight:600;color:#1a1a2e;\">${店铺}</span>" +
            "    <span style=\"margin-left:auto;font-size:12px;color:#fff;background:#ff6b6b;border-radius:10px;padding:2px 8px;\">${平台}</span>" +
            "  </div>" +
            "  <div style=\"background:#fff;border-radius:8px;padding:12px;\">" +
            "    <div style=\"display:flex;justify-content:space-between;margin-bottom:8px;\">" +
            "      <span style=\"font-size:13px;color:#8c8c8c;\">返现规则</span>" +
            "      <span style=\"font-size:13px;color:#ff6b6b;font-weight:600;\">${规则}</span>" +
            "    </div>" +
            "    <div style=\"display:flex;justify-content:space-between;margin-bottom:8px;\">" +
            "      <span style=\"font-size:13px;color:#8c8c8c;\">剩余库存</span>" +
            "      <span style=\"font-size:13px;color:#1a1a2e;font-weight:500;\">${库存}</span>" +
            "    </div>" +
            "    <div style=\"display:flex;justify-content:space-between;margin-bottom:8px;\">" +
            "      <span style=\"font-size:13px;color:#8c8c8c;\">活动时间</span>" +
            "      <span style=\"font-size:13px;color:#1a1a2e;\">${开始时间} ~ ${结束时间}</span>" +
            "    </div>" +
            "    <div style=\"display:flex;justify-content:space-between;margin-bottom:8px;\">" +
            "      <span style=\"font-size:13px;color:#8c8c8c;\">距离</span>" +
            "      <span style=\"font-size:13px;color:#1a1a2e;\">${距离}</span>" +
            "    </div>" +
            "    <div style=\"display:flex;justify-content:space-between;\">" +
            "      <span style=\"font-size:13px;color:#8c8c8c;\">评价要求</span>" +
            "      <span style=\"font-size:13px;color:#1a1a2e;\">${评价条件}</span>" +
            "    </div>" +
            "  </div>" +
            "  <div style=\"margin-top:10px;display:flex;align-items:center;\">" +
            "    <span style=\"font-size:12px;color:#b0b0b0;\">📍 ${地址}</span>" +
            "    <span style=\"margin-left:8px;font-size:11px;color:#b0b0b0;background:#f0f0f0;border-radius:4px;padding:1px 6px;\">${门店类型}</span>" +
            "  </div>" +
            "</div>";
    /**
     * 合并发送时的 summary 模板
     */
    private static final String MERGED_SUMMARY_TEMPLATE = "${类型}共有${数量}个新返现活动";

    /**
     * 将消息加入缓冲队列，延迟1分钟后按 spt + MonitorTypeEnums 分组合并发送。
     *
     * @param notifyConfig   监控配置
     * @param storeInfos     满足条件的门店列表
     * @param locationEntity 位置信息
     */
    public void queueMessage(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos, LocationEntity locationEntity) {
        UserEntity userEntity = userService.getById(locationEntity.getUserId());
        if (userEntity == null || !StringUtils.hasText(userEntity.getSpt())) {
            log.warn("用户不存在或spt为空，跳过消息入队 userId:{}", locationEntity.getUserId());
            return;
        }
        String spt = userEntity.getSpt();
        MonitorTypeEnums monitorType = notifyConfig.getType();
        String batchKey = spt + ":" + monitorType.name();

        // 构建当前批次的消息内容
        List<String> parts = storeInfos.stream()
                .map(storeInfo -> buildStoreMessage(storeInfo, locationEntity))
                .collect(Collectors.toCollection(ArrayList::new));
        int count = storeInfos.size();

        boolean shouldSchedule;
        synchronized (pendingBatches) {
            MessageBatch batch = pendingBatches.get(batchKey);
            if (batch == null) {
                batch = new MessageBatch(spt, monitorType);
                pendingBatches.put(batchKey, batch);
                shouldSchedule = true;
            } else {
                shouldSchedule = false;
            }
            batch.add(parts, count);
        }

        if (shouldSchedule) {
            scheduler.schedule(() -> flushBatch(batchKey), DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * 刷新指定分组的缓冲消息，合并后发送
     */
    private void flushBatch(String batchKey) {
        MessageBatch batch;
        synchronized (pendingBatches) {
            batch = pendingBatches.remove(batchKey);
        }
        if (batch == null || batch.isEmpty()) {
            return;
        }

        // 合并所有消息内容
        String body = String.join("<br/><br/>", batch.messageParts);
        String summary = buildMergedSummary(batch);

        try {
            log.info("批量发送消息 spt:{}, monitorType:{}, 共{}个门店",
                    batch.spt, batch.monitorType, batch.storeCount);
            sptService.sendMessage(batch.spt, body, summary);
        } catch (Exception e) {
            log.error("批量发送消息失败 spt:{}, monitorType:{}", batch.spt, batch.monitorType, e);
        }
    }

    /**
     * 构建合并后的消息摘要
     */
    private String buildMergedSummary(MessageBatch batch) {
        return MERGED_SUMMARY_TEMPLATE
                .replace("${类型}", batch.monitorType.getDescription())
                .replace("${数量}", String.valueOf(batch.storeCount));
    }

    /**
     * 构建单个门店的消息内容
     */
    private String buildStoreMessage(StoreInfo storeInfo, LocationEntity locationEntity) {
        String rebateConditionText = storeInfo.getRebateConditionStr() == null ? "未知" : storeInfo.getRebateConditionStr();
        String storeTypeText = storeInfo.getStoreTypeEnum() == null ? "未知" : storeInfo.getStoreTypeEnum().getDescription();
        return DEFAULT_BODY_TEMPLATE
                .replace("${地址}", locationEntity.getName())
                .replace("${平台}", StorePlatformEnum.getByType(storeInfo.getType()).name)
                .replace("${门店类型}", storeTypeText)
                .replace("${店铺}", storeInfo.getName())
                .replace("${开始时间}", storeInfo.getStartTime())
                .replace("${结束时间}", storeInfo.getEndTime())
                .replace("${距离}", storeInfo.getDistanceStr() == null ? "未知" : storeInfo.getDistanceStr())
                .replace("${库存}", String.valueOf(storeInfo.getLeftNumber()))
                .replace("${规则}", buildRuleText(storeInfo))
                .replace("${评价条件}", rebateConditionText);
    }

    /**
     * 构建返现规则文案，兼容满减和百分比返现（美团赏金）两类数据
     */
    private String buildRuleText(StoreInfo storeInfo) {
        if (storeInfo.getRebateRatio() != null) {
            String ruleText = "返现" + storeInfo.getRebateRatio().stripTrailingZeros().toPlainString() + "%";
            if (storeInfo.getRebateMax() != null) {
                ruleText += "，最高返" + storeInfo.getRebateMax().stripTrailingZeros().toPlainString() + "元";
            }
            return ruleText;
        }
        if (storeInfo.getPrice() != null && storeInfo.getRebatePrice() != null) {
            return "满" + storeInfo.getPrice().toPlainString() + "返" + storeInfo.getRebatePrice().toPlainString();
        }
        return "未知";
    }

    /**
     * 消息批次，按 spt + MonitorTypeEnums 分组
     */
    private static class MessageBatch {
        final String spt;
        final MonitorTypeEnums monitorType;
        final List<String> messageParts = new ArrayList<>();
        int storeCount = 0;

        MessageBatch(String spt, MonitorTypeEnums monitorType) {
            this.spt = spt;
            this.monitorType = monitorType;
        }

        void add(List<String> parts, int count) {
            messageParts.addAll(parts);
            storeCount += count;
        }

        boolean isEmpty() {
            return messageParts.isEmpty();
        }
    }
}
