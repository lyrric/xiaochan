package io.github.xiaochan;

import io.github.xiaocan.http.MessageHttp;
import io.github.xiaocan.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class IframeTest {

    private static final String SPT = "x";



    @Test
    void testSendMergedCards() {

        String body = """
                <iframe src="xxxxx/s/77de35a2d499416f/1" width="100%" height="1000px" frameborder="0" allowfullscreen></iframe>
                """;
        MessageHttp.sendMessage(SPT, body, "最低实付共有3个新返现活动");
        log.info("合并卡片消息发送完成");
    }


}
