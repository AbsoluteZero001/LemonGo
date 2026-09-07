package com.lemongo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lemongo")
public class LemonGoProperties {

    private String requestIdHeader = "X-Request-Id";
    private String appVersion = "0.1.0";
}

