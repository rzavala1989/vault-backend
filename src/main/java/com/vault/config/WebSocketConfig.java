package com.vault.config;

import com.vault.websocket.BarWebSocketHandler;
import com.vault.websocket.QuoteWebSocketHandler;
import com.vault.websocket.TradeWebSocketHandler;
import com.vault.websocket.WsTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final QuoteWebSocketHandler quoteHandler;
    private final BarWebSocketHandler barHandler;
    private final TradeWebSocketHandler tradeHandler;
    private final WsTokenInterceptor tokenInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(quoteHandler, "/aq/get-quote")
                .addInterceptors(tokenInterceptor)
                .setAllowedOrigins("*");

        registry.addHandler(barHandler, "/ab/get-bar")
                .addInterceptors(tokenInterceptor)
                .setAllowedOrigins("*");

        registry.addHandler(tradeHandler, "/at/get-trade")
                .addInterceptors(tokenInterceptor)
                .setAllowedOrigins("*");
    }
}
