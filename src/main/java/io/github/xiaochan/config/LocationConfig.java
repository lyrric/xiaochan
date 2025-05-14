package io.github.xiaochan.config;

import io.github.xiaochan.model.Location;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "config")
@Data
public class LocationConfig {

    /**
     * 配置
     */
    private List<Location> locations;


}
