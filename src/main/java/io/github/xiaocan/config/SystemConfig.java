package io.github.xiaocan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * @author lyrric
 * @date 2026/8/6
 */
@ConfigurationProperties(prefix = "system")
public class SystemConfig {

    /**
     * webUrl，末尾不带 /
     */
    private String webUrl;
}
