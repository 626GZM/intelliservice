package com.intelliservice.backend.config;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${agent.service.url}")
    private String agentServiceUrl;

    @Value("${deepseek.api-key}")
    private String deepseekApiKey;

    @Bean
    public WebClient agentWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(120));
        log.info("Agent WebClient baseUrl={} responseTimeout=120s", agentServiceUrl);
        return WebClient.builder()
                .baseUrl(agentServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
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
