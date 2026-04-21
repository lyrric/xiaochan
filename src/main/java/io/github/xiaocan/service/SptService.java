package io.github.xiaocan.service;

import io.github.xiaocan.http.MessageHttp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangxiaodong
 * @date 2026/4/15
 */
@Slf4j
@Service
public class SptService {


    private final Map<String, String> codeMap = new ConcurrentHashMap<>();


    private static final String SPT_CODE_KEY_PREFIX = "spt:code:";
    private static final int CODE_LENGTH = 6;

    /**
     * 发送验证码
     *
     * @param spt 用户spt标识
     */
    public void sendSptCode(String spt) {
        // 生成6位数字验证码
        String code = generateCode();

        // 存储到Redis，5分钟过期
        String key = SPT_CODE_KEY_PREFIX + spt;
        codeMap.put(key, code);

        // 发送消息
        String content = "您的验证码是: " + code + "，有效期5分钟，请勿泄露给他人。";
        String summary = "验证码通知";
        MessageHttp.sendMessage(spt, content, summary);

        log.info("已向 spt:{} 发送验证码", spt);
    }

    /**
     * 校验验证码
     *
     * @param spt  用户spt标识
     * @param code 验证码
     * @return 校验是否通过
     */
    public boolean checkSptCode(String spt, String code) {
        if (spt == null || code == null) {
            return false;
        }

        String key = SPT_CODE_KEY_PREFIX + spt;
        String storedCode = codeMap.get(key);

        if (storedCode == null) {
            log.warn("spt:{} 验证码已过期或不存在", spt);
            return false;
        }

        if (storedCode.equals(code)) {
            // 校验成功后删除验证码
            codeMap.remove(key);
            log.info("spt:{} 验证码校验成功", spt);
            return true;
        } else {
            log.warn("spt:{} 验证码校验失败", spt);
            return false;
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random(System.currentTimeMillis());
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
