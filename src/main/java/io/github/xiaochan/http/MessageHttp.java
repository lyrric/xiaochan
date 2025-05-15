package io.github.xiaochan.http;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MessageHttp {



    public static void sendMessage(String spt, String content, String summary){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("content", content);
        bodyMap.put("summary", summary);
        bodyMap.put("contentType", 2);
        bodyMap.put("spt", spt);
        try {
            String resBody = HttpUtil.post("https://wxpusher.zjiecode.com/api/send/message/simple-push", JSONObject.toJSONString(bodyMap));
            JSONObject jsonObject = JSONObject.parseObject(resBody);
            if (jsonObject.getInteger("code") != 1000) {
                log.error("发送消息失败: {}", resBody);
            }
        }catch (Exception e){
            log.error("发送消息失败: {}", e.getMessage());
        }

    }

}
