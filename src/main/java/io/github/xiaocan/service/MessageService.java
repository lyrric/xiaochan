package io.github.xiaocan.service;

import io.github.xiaocan.config.SystemConfig;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Resource
    private SystemConfig systemConfig;
    @Resource
    private MessageBatchRecordService messageBatchRecordService;

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
     * 卡片模板（与 MessageService 中一致）
     */
    public static final String DEFAULT_BODY_TEMPLATE =
            """
            <div style="border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;margin:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
              <div style="height:4px;background:linear-gradient(90deg,#4f6ef7,#a78bfa,#f472b6);"></div>
              <div style="padding:16px;">
                <table cellspacing="0" cellpadding="0" border="0" style="border-collapse:collapse;margin-bottom:12px;"><tr>
                  <td style="vertical-align:top;padding:0;padding-right:12px;">
                    <img src="${图标}" width="48" height="48" style="display:block !important;max-width:48px !important;width:48px !important;height:48px !important;margin:0 !important;border-radius:8px !important;object-fit:cover !important;" />
                  </td>
                  <td style="vertical-align:top;padding:0;">
                    <div style="font-size:16px;font-weight:700;color:#1a1a2e;margin-bottom:6px;line-height:1.4;">${店铺}</div>
                    <div>
                      <span style="display:inline-block;font-size:11px;color:#856404;background:#fff3cd;border-radius:999px;padding:2px 8px;">${平台}</span>
                      <span style="display:inline-block;font-size:11px;color:#7c3aed;background:#f3e8ff;border-radius:999px;padding:2px 8px;margin-left:4px;">${门店类型}</span>
                      <span style="display:inline-block;font-size:11px;color:#9ca3af;margin-left:6px;">📍 ${距离} · ${地址}</span>
                    </div>
                  </td>
                </tr></table>
                <div style="border-top:1px solid #f3f4f6;padding-top:10px;margin-bottom:8px;">
                  <span style="display:inline-block;font-size:12px;color:#92400e;background:#fef3c7;border-radius:999px;padding:3px 8px;white-space:nowrap;">🏷️ ${规则}</span>
                  <span style="display:inline-block;font-size:12px;color:#6b7280;background:#f3f4f6;border-radius:999px;padding:3px 8px;margin-left:4px;white-space:nowrap;">🕐 ${开始时间}-${结束时间}</span>
                  <span style="display:inline-block;font-size:12px;color:#166534;background:#dcfce7;border-radius:999px;padding:3px 8px;margin-left:4px;white-space:nowrap;">📦 ${库存}</span>
                  <span style="display:inline-block;font-size:12px;color:#6b7280;background:#f3f4f6;border-radius:999px;padding:3px 8px;margin-left:4px;white-space:nowrap;">📝 ${评价条件}</span>
                </div>
              </div>
            </div>
            """;
    String IFRAME_BODY = """
                <iframe src="${WEB_URL}/s/${TOKEN}/${MSG_ID}" width="100%" height="1000px" frameborder="0" allowfullscreen></iframe>
                """;
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
     * @param batchId        推送批次ID
     */
    public void queueMessage(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos, LocationEntity locationEntity, String batchId) {
        UserEntity userEntity = userService.getById(locationEntity.getUserId());
        if (userEntity == null || !StringUtils.hasText(userEntity.getSpt())) {
            log.warn("用户不存在或spt为空，跳过消息入队 userId:{}", locationEntity.getUserId());
            return;
        }
        String spt = userEntity.getSpt();
        String token = userEntity.getToken();
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
                batch = new MessageBatch(spt, monitorType, locationEntity.getUserId(), token);
                pendingBatches.put(batchKey, batch);
                shouldSchedule = true;
            } else {
                shouldSchedule = false;
            }
            batch.add(parts, count, batchId);
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
        // 记录消息批次
        String batchIds = String.join(",", batch.batchIds);
        Long msgId = messageBatchRecordService.recordBatch(batch.userId, batchIds);

        // 合并所有消息内容
        String body;
        if (StringUtils.hasText(systemConfig.getWebUrl())) {
            log.info("使用iframe方式发送消息 msgId:{} webUrl:{}", msgId, systemConfig.getWebUrl());
            body = IFRAME_BODY.replace("${WEB_URL}", systemConfig.getWebUrl())
                    .replace("${TOKEN}", batch.token)
                    .replace("${MSG_ID}", String.valueOf(msgId));
        }else{
            body = String.join("<br/><br/>", batch.messageParts);
        }

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
        String iconUrl = storeInfo.getIcon() == null ? "" : storeInfo.getIcon();
        return DEFAULT_BODY_TEMPLATE
                .replace("${图标}", iconUrl)
                .replace("${地址}", locationEntity.getName())
                .replace("${平台}", StorePlatformEnum.getByType(storeInfo.getType()).name)
                .replace("${门店类型}", storeTypeText)
                .replace("${店铺}", storeInfo.getName())
                .replace("${开始时间}", storeInfo.getStartTime())
                .replace("${结束时间}", storeInfo.getEndTime())
                .replace("${距离}", storeInfo.getDistanceStr() == null ? "未知" : storeInfo.getDistanceStr())
                .replace("${库存}", "剩余 " + storeInfo.getLeftNumber())
                .replace("${规则}", buildRuleText(storeInfo))
                .replace("${评价条件}", rebateConditionText);
    }


    /**
     * 构建返现规则文案，兼容满减和百分比返现（美团赏金）两类数据
     */
    private String buildRuleText(StoreInfo storeInfo) {
        if (storeInfo.getRebateRatio() != null) {
            String ruleText = "返" + storeInfo.getRebateRatio().stripTrailingZeros().toPlainString() + "%";
            if (storeInfo.getRebateMax() != null) {
                ruleText += "最高" + storeInfo.getRebateMax().stripTrailingZeros().toPlainString() ;
            }
            return ruleText;
        }
        if (storeInfo.getPrice() != null && storeInfo.getRebatePrice() != null) {
            return "满" + storeInfo.getPrice().stripTrailingZeros().toPlainString() + "返" + storeInfo.getRebatePrice().stripTrailingZeros().toPlainString();
        }
        return "未知";
    }

    /**
     * 消息批次，按 spt + MonitorTypeEnums 分组
     */
    private static class MessageBatch {
        final String spt;
        final MonitorTypeEnums monitorType;
        final Integer userId;
        final List<String> messageParts = new ArrayList<>();
        final Set<String> batchIds = new HashSet<>();
        final String token;
        int storeCount = 0;

        MessageBatch(String spt, MonitorTypeEnums monitorType, Integer userId,String token) {
            this.spt = spt;
            this.monitorType = monitorType;
            this.userId = userId;
            this.token = token;
        }

        void add(List<String> parts, int count, String batchId) {
            messageParts.addAll(parts);
            storeCount += count;
            if (batchId != null) {
                batchIds.add(batchId);
            }
        }

        boolean isEmpty() {
            return messageParts.isEmpty();
        }
    }
}
