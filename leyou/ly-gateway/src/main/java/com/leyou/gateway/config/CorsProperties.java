package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "ly.cors")
public class CorsProperties {

    private List<String> allowedOrigins;
    private Boolean allowedCredentials;
    private List<String> allowedHeaders;
    private List<String> allowedMethods;
    private Long maxAge;
    private String filterPath;
}
