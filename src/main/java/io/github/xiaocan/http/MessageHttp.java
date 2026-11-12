package io.github.xiaocan.http;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.xiaocan.config.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MessageHttp {



    public static void sendMessage(String spt, String content, String summary){
        try {
            sendMessageOrThrow(spt, content, summary);
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 发送消息并将网络或推送平台错误交由手动操作的调用方处理。
     */
    public static void sendMessageOrThrow(String spt, String content, String summary) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("content", content);
        bodyMap.put("summary", summary);
        bodyMap.put("contentType", 2);
        bodyMap.put("spt", spt);
        String resBody = HttpUtil.post("https://wxpusher.zjiecode.com/api/send/message/simple-push", JSONObject.toJSONString(bodyMap));
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        if (jsonObject == null || !Integer.valueOf(1000).equals(jsonObject.getInteger("code"))) {
            log.error("发送消息失败: {}", resBody);
            throw new BusinessException("推送平台返回失败");
        }

    }

}
