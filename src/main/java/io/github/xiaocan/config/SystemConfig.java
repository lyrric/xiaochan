package io.github.xiaocan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lyrric
 * @date 2026/8/6
 */
@Component
@ConfigurationProperties(prefix = "system")
public class SystemConfig {

    /**
     * webUrl，末尾不带 /
     */
    private String webUrl;

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
