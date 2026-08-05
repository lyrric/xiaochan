package io.github.xiaochan;

import io.github.xiaocan.http.MessageHttp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class XiaochanTest {

    private static final String SPT = "xxx";

    /**
     * 卡片模板（与 MessageService 中一致）
     */
    private static final String CARD_TEMPLATE =
            "<div style=\"border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;margin:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;\">" +
            "  <div style=\"height:4px;background:linear-gradient(90deg,#4f6ef7,#a78bfa,#f472b6);\"></div>" +
            "  <div style=\"padding:16px;\">" +
            "    <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"border-collapse:collapse;margin-bottom:12px;\"><tr>" +
            "      <td style=\"vertical-align:top;padding:0;padding-right:12px;\">" +
            "        <img src=\"${图标}\" width=\"48\" height=\"48\" style=\"display:block !important;max-width:48px !important;width:48px !important;height:48px !important;margin:0 !important;border-radius:8px !important;object-fit:cover !important;\" />" +
            "      </td>" +
            "      <td style=\"vertical-align:top;padding:0;\">" +
            "        <div style=\"font-size:16px;font-weight:700;color:#1a1a2e;margin-bottom:6px;line-height:1.4;\">${店铺}</div>" +
            "        <div>" +
            "          <span style=\"display:inline-block;font-size:11px;color:#856404;background:#fff3cd;border-radius:999px;padding:2px 8px;\">${平台}</span>" +
            "          <span style=\"display:inline-block;font-size:11px;color:#7c3aed;background:#f3e8ff;border-radius:999px;padding:2px 8px;margin-left:4px;\">${门店类型}</span>" +
            "          <span style=\"display:inline-block;font-size:11px;color:#9ca3af;margin-left:6px;\">📍 ${距离} · ${地址}</span>" +
            "        </div>" +
            "      </td>" +
            "    </tr></table>" +
            "    <div style=\"background:#fef2f2;border-radius:8px;padding:10px 12px;margin-bottom:12px;\">" +
            "      <span style=\"font-size:12px;color:#991b1b;\">🏷️ </span>" +
            "      <span style=\"font-size:20px;font-weight:700;color:#ef4444;\">${规则}</span>" +
            "    </div>" +
            "    <div style=\"border-top:1px solid #f3f4f6;padding-top:10px;margin-bottom:8px;\">" +
            "      <span style=\"display:inline-block;font-size:12px;color:#6b7280;background:#f3f4f6;border-radius:999px;padding:3px 8px;white-space:nowrap;\">🕐 ${开始时间}-${结束时间}</span>" +
            "      <span style=\"display:inline-block;font-size:12px;color:#166534;background:#dcfce7;border-radius:999px;padding:3px 8px;margin-left:4px;white-space:nowrap;\">📦 ${库存}</span>" +
            "      <span style=\"display:inline-block;font-size:12px;color:#6b7280;background:#f3f4f6;border-radius:999px;padding:3px 8px;margin-left:4px;white-space:nowrap;\">📝 ${评价条件}</span>" +
            "    </div>" +
            "  </div>" +
            "</div>";



    @Test
    void testSendMergedCards() {
        String card1 = buildCard(
                "http://p0.meituan.net/waimaipoi/c2a4d9f99a89ed502518bcc9ba31107c73955.jpg.webp",
                "瑞幸咖啡(望京SOHO店)", "美团", "小蚕美团赏金",
                "返现5%，最高返3元", 128, "08:00", "21:00", "1.2km", "无需评价", "公司");
        String card2 = buildCard(
                "http://p0.meituan.net/business/73c692b83345450d20991c834ea75a4c1250604.png",
                "蜜雪冰城(阜通西大街店)", "饿了么", "小蚕满减",
                "满20返5", 56, "10:00", "22:00", "850m", "好评返现", "公司");
        String card3 = buildCard(
                "http://p1.meituan.net/farmer/ae7e9c86082256b7156f0ebe8d174023302687.png",
                "肯德基(望京店)", "美团", "小蚕美团赏金",
                "返现8%，最高返5元", 32, "09:00", "20:00", "2.5km", "需五星好评", "公司");

        String body = String.join("<br/><br/>", card1, card2, card3);
        MessageHttp.sendMessage(SPT, body, "最低实付共有3个新返现活动");
        log.info("合并卡片消息发送完成");
    }

    private String buildCard(String icon, String store, String platform, String storeType,
                             String rule, int leftNumber, String startTime, String endTime,
                             String distance, String reviewCondition, String address) {
        return CARD_TEMPLATE
                .replace("${图标}", icon)
                .replace("${店铺}", store)
                .replace("${平台}", platform)
                .replace("${门店类型}", storeType)
                .replace("${规则}", rule)
                .replace("${库存}", "剩余 " + leftNumber)
                .replace("${开始时间}", startTime)
                .replace("${结束时间}", endTime)
                .replace("${距离}", distance)
                .replace("${评价条件}", reviewCondition)
                .replace("${地址}", address);
    }

}
