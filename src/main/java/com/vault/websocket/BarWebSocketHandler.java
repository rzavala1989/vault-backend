package com.vault.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vault.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BarWebSocketHandler extends TextWebSocketHandler {

    private final MarketDataService marketDataService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<WebSocketSession, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = mapper.readTree(message.getPayload());
        String action = payload.has("action") ? payload.get("action").asText() : "";
        String asset = payload.has("asset") ? payload.get("asset").asText() : "";

        if ("subscribe".equals(action)) {
            marketDataService.subscribe(session, asset);
            if (!tasks.containsKey(session)) {
                ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                        () -> sendBars(session), 1000, 1000, TimeUnit.MILLISECONDS);
                tasks.put(session, future);
            }
        } else if ("unsubscribe".equals(action)) {
            marketDataService.unsubscribe(session, asset);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ScheduledFuture<?> future = tasks.remove(session);
        if (future != null) future.cancel(false);
        marketDataService.removeSession(session);
    }

    private void sendBars(WebSocketSession session) {
        if (!session.isOpen()) {
            ScheduledFuture<?> future = tasks.remove(session);
            if (future != null) future.cancel(false);
            return;
        }
        Set<String> symbols = marketDataService.getSubscriptions().get(session);
        if (symbols == null) return;
        for (String symbol : symbols) {
            try {
                ObjectNode bar = marketDataService.generateBarTick(symbol);
                session.sendMessage(new TextMessage(mapper.writeValueAsString(bar)));
            } catch (IOException e) {
                log.debug("Failed to send bar to session {}", session.getId());
            }
        }
    }
}
