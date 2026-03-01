package com.vault.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final VaultProperties props;

    @Bean
    public WebClient alpacaBrokerClient() {
        String credentials = props.getAlpaca().getApiKey() + ":" + props.getAlpaca().getApiSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        return WebClient.builder()
                .baseUrl(props.getAlpaca().getBrokerBaseUrl())
                .defaultHeader("Authorization", "Basic " + encoded)
                .build();
    }

    @Bean
    public WebClient alpacaMarketsClient() {
        String credentials = props.getAlpaca().getApiKey() + ":" + props.getAlpaca().getApiSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        return WebClient.builder()
                .baseUrl(props.getAlpaca().getMarketsBaseUrl())
                .defaultHeader("Authorization", "Basic " + encoded)
                .build();
    }

    @Bean
    public WebClient finnhubClient() {
        return WebClient.builder()
                .baseUrl(props.getFinnhub().getBaseUrl())
                .defaultHeader("X-Finnhub-Token", props.getFinnhub().getApiKey())
                .build();
    }
}
