package io.github.xiaochan;

import io.github.xiaocan.http.MessageHttp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class XiaochanTest {

    /**
     * TODO: 替换为你自己的 spt
     */
    private static final String SPT = "替换为你自己的 spt";

    /**
     * 卡片模板（与 MessageService 中一致）
     */
    private static final String CARD_TEMPLATE =
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
     * 测试发送单张卡片消息
     */
    @Test
    void testSendSingleCard() {
        String content = buildCard("瑞幸咖啡(望京SOHO店)", "美团", "小蚕美团赏金",
                "返现5%，最高返3元", "128", "08:00", "21:00", "1.2km", "无需评价", "公司");

        MessageHttp.sendMessage(SPT, content, "共有1个新返现活动");
        log.info("单张卡片消息发送完成");
    }

    /**
     * 测试发送多张卡片合并消息（模拟批量合并场景）
     */
    @Test
    void testSendMergedCards() {
        String card1 = buildCard("瑞幸咖啡(望京SOHO店)", "美团", "小蚕美团赏金",
                "返现5%，最高返3元", "128", "08:00", "21:00", "1.2km", "无需评价", "公司");
        String card2 = buildCard("蜜雪冰城(阜通西大街店)", "饿了么", "小蚕满减",
                "满20返5", "56", "10:00", "22:00", "850m", "好评返现", "公司");
        String card3 = buildCard("肯德基(望京店)", "美团", "小蚕美团赏金",
                "返现8%，最高返5元", "32", "09:00", "20:00", "2.5km", "需五星好评", "公司");

        String body = String.join("<br/><br/>", card1, card2, card3);
        MessageHttp.sendMessage(SPT, body, "共有3个新返现活动");
        log.info("合并卡片消息发送完成");
    }

    private String buildCard(String store, String platform, String storeType,
                             String rule, String stock, String startTime, String endTime,
                             String distance, String reviewCondition, String address) {
        return CARD_TEMPLATE
                .replace("${店铺}", store)
                .replace("${平台}", platform)
                .replace("${门店类型}", storeType)
                .replace("${规则}", rule)
                .replace("${库存}", stock)
                .replace("${开始时间}", startTime)
                .replace("${结束时间}", endTime)
                .replace("${距离}", distance)
                .replace("${评价条件}", reviewCondition)
                .replace("${地址}", address);
    }

}
