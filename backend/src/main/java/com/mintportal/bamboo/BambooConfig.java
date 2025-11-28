package com.mintportal.bamboo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bamboo")
@Getter
@Setter
public class BambooConfig {

    private String baseUrl;
    private String apiToken;
    private String username;
    private String password;
    private long pollingInterval = 30000; // 30 seconds default
}
