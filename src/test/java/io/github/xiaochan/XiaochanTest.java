package io.github.xiaochan;

import io.github.xiaocan.http.MessageHttp;
import io.github.xiaocan.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class XiaochanTest {

    private static final String SPT = "x";



    @Test
    void testSendMergedCards() {
        String card1 = buildCard(
                "http://p0.meituan.net/waimaipoi/c2a4d9f99a89ed502518bcc9ba31107c73955.jpg.webp",
                "瑞幸咖啡(望京SOHO店)", "美团", "小蚕美团赏金",
                "返5%最高3", 128, "08:00", "21:00", "1.2km", "无需评价", "公司");
        String card2 = buildCard(
                "http://p0.meituan.net/business/73c692b83345450d20991c834ea75a4c1250604.png",
                "蜜雪冰城(阜通西大街店)", "饿了么", "小蚕满减",
                "满20返5", 56, "10:00", "22:00", "850m", "好评返现", "公司");
        String card3 = buildCard(
                "http://p1.meituan.net/farmer/ae7e9c86082256b7156f0ebe8d174023302687.png",
                "肯德基(望京店)", "美团", "小蚕美团赏金",
                "返8%最高5", 32, "09:00", "20:00", "2.5km", "需五星好评", "公司");

        String body = String.join("<br/><br/>", card1, card2, card3);
        MessageHttp.sendMessage(SPT, body, "最低实付共有3个新返现活动");
        log.info("合并卡片消息发送完成");
    }

    private String buildCard(String icon, String store, String platform, String storeType,
                             String rule, int leftNumber, String startTime, String endTime,
                             String distance, String reviewCondition, String address) {
        return MessageService.DEFAULT_BODY_TEMPLATE
                .replace("${图标}", icon)
                .replace("${店铺}", store)
                .replace("${平台}", platform)
                .replace("${门店类型}", storeType)
                .replace("${规则}", rule)
                .replace("${库存}", String.valueOf(leftNumber))
                .replace("${开始时间}", startTime)
                .replace("${结束时间}", endTime)
                .replace("${距离}", distance)
                .replace("${评价条件}", reviewCondition)
                .replace("${地址}", address);
    }

}
