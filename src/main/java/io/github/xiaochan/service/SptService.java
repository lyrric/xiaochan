package io.github.xiaochan.service;

import io.github.xiaochan.http.MessageHttp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author wangxiaodong
 * @date 2026/4/15
 */
@Slf4j
@Service
public class SptService {

    @Resource
    private RedissonClient redissonClient;

    private static final String SPT_CODE_KEY_PREFIX = "spt:code:";
    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRE_MINUTES = 5;

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
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(code, Duration.ofMinutes(CODE_EXPIRE_MINUTES));

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
        RBucket<String> bucket = redissonClient.getBucket(key);
        String storedCode = bucket.get();

        if (storedCode == null) {
            log.warn("spt:{} 验证码已过期或不存在", spt);
            return false;
        }

        if (storedCode.equals(code)) {
            // 校验成功后删除验证码
            bucket.delete();
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
