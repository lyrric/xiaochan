package io.github.xiaocan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wangxiaodong
 * @date 2026/10/16
 */
@Component
@ConfigurationProperties(prefix = "proxy.xiequ")
@Data
public class XieQuProxyConfig {

    /**
     * 是否启用
     */
    private Boolean enable = false;

    /**
     * ukey:ip白名单操作用
     */
    private String ukey;

    /**
     * 获取ip的url参数
     * 例如：act=getturn51&uid=49226&vkey=xxxxx&num=5&time=6&plat=0&re=0&type=7&so=1&group=51&ow=1&spl=1&addr=&db=1
     */
    private String getIpParams;

    /**
     * uid
     */
    private String uid;

}
