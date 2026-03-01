package com.vault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vault")
public class VaultProperties {

    private Jwt jwt = new Jwt();
    private Alpaca alpaca = new Alpaca();
    private Finnhub finnhub = new Finnhub();
    private Websocket websocket = new Websocket();
    private FileStorage fileStorage = new FileStorage();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs;
        private long refreshExpirationMs;
    }

    @Getter
    @Setter
    public static class Alpaca {
        private String brokerBaseUrl;
        private String marketsBaseUrl;
        private String streamBaseUrl;
        private String apiKey;
        private String apiSecret;
    }

    @Getter
    @Setter
    public static class Finnhub {
        private String baseUrl;
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Websocket {
        private String mode;
        private int simulatedTickIntervalMs;
    }

    @Getter
    @Setter
    public static class FileStorage {
        private String basePath;
    }
}
