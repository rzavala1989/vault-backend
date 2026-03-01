package com.vault.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vault.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteWebSocketHandler extends TextWebSocketHandler {

    private final MarketDataService marketDataService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = mapper.readTree(message.getPayload());
        String action = payload.has("action") ? payload.get("action").asText() : "";
        String asset = payload.has("asset") ? payload.get("asset").asText() : "";

        switch (action) {
            case "subscribe" -> marketDataService.subscribe(session, asset);
            case "unsubscribe" -> marketDataService.unsubscribe(session, asset);
            default -> log.warn("Unknown WS action: {}", action);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        marketDataService.removeSession(session);
    }
}
