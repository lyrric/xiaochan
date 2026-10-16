package io.github.xiaocan.model;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

import java.util.Date;

/**
 * Created on 2020-10-30.
 * ip代理信息
 * @author wangxiaodong
 */
@Data
public class HttpProxyInfo {

    /**
     * 代理ip地址
     */
    private String ip;
    /**
     * 代理端口
     */
    private Integer port;
    /**
     * 过期时间
     */
    private Date expiry;
    /**
     * 代理方式http或者https
     */
    private String scheme;

    private RateLimiter rateLimiter = RateLimiter.create(6);
}
