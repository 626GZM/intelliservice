package com.intelliservice.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${agent.service.url}")
    private String agentServiceUrl;

    @Value("${deepseek.api-key}")
    private String deepseekApiKey;

    @Bean
    public WebClient agentWebClient() {
        return WebClient.builder()
                .baseUrl(agentServiceUrl)
                .build();
    }

    @Bean
    public WebClient deepseekWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.deepseek.com/v1")
                .defaultHeader("Authorization", "Bearer " + deepseekApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
