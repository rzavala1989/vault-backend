package com.vault.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vault.config.VaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
@Service
public class MarketDataService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Double> lastPrices = new ConcurrentHashMap<>();
    private final boolean simulated;

    public MarketDataService(VaultProperties props) {
        this.simulated = "simulated".equalsIgnoreCase(props.getWebsocket().getMode());

        if (simulated) {
            int interval = props.getWebsocket().getSimulatedTickIntervalMs();
            scheduler.scheduleAtFixedRate(this::emitSimulatedTicks, interval, interval, TimeUnit.MILLISECONDS);
            log.info("MarketDataService running in SIMULATED mode ({}ms interval)", interval);
        } else {
            log.info("MarketDataService running in LIVE mode");
        }
    }

    public void subscribe(WebSocketSession session, String symbol) {
        subscriptions.computeIfAbsent(session, k -> ConcurrentHashMap.newKeySet()).add(symbol.toUpperCase());
        lastPrices.putIfAbsent(symbol.toUpperCase(), 150.0 + ThreadLocalRandom.current().nextDouble(100));
        log.debug("Session {} subscribed to {}", session.getId(), symbol);
    }

    public void unsubscribe(WebSocketSession session, String symbol) {
        Set<String> symbols = subscriptions.get(session);
        if (symbols != null) {
            symbols.remove(symbol.toUpperCase());
        }
    }

    public void removeSession(WebSocketSession session) {
        subscriptions.remove(session);
    }

    public ObjectNode generateQuoteTick(String symbol) {
        double price = getNextPrice(symbol);
        double spread = price * 0.001;
        ObjectNode node = mapper.createObjectNode();
        node.put("S", symbol);
        node.put("bp", round(price - spread));
        node.put("ap", round(price + spread));
        node.put("bs", ThreadLocalRandom.current().nextInt(1, 500));
        node.put("as", ThreadLocalRandom.current().nextInt(1, 500));
        node.put("t", Instant.now().toString());
        return node;
    }

    public ObjectNode generateBarTick(String symbol) {
        double price = getNextPrice(symbol);
        double range = price * 0.005;
        ObjectNode node = mapper.createObjectNode();
        node.put("S", symbol);
        node.put("o", round(price - range));
        node.put("h", round(price + range));
        node.put("l", round(price - range * 1.5));
        node.put("c", round(price));
        node.put("v", ThreadLocalRandom.current().nextInt(1000, 50000));
        node.put("t", Instant.now().toString());
        node.put("n", ThreadLocalRandom.current().nextInt(10, 500));
        node.put("vw", round(price));
        return node;
    }

    public ObjectNode generateTradeTick(String symbol) {
        double price = getNextPrice(symbol);
        ObjectNode node = mapper.createObjectNode();
        node.put("S", symbol);
        node.put("p", round(price));
        node.put("s", ThreadLocalRandom.current().nextInt(1, 200));
        node.put("t", Instant.now().toString());
        node.put("x", "V");
        node.put("z", "A");
        return node;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public Map<WebSocketSession, Set<String>> getSubscriptions() {
        return subscriptions;
    }

    private void emitSimulatedTicks() {
        subscriptions.forEach((session, symbols) -> {
            if (!session.isOpen()) {
                subscriptions.remove(session);
                return;
            }
            for (String symbol : symbols) {
                try {
                    ObjectNode tick = generateQuoteTick(symbol);
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(tick)));
                } catch (IOException e) {
                    log.debug("Failed to send tick to session {}", session.getId());
                    subscriptions.remove(session);
                }
            }
        });
    }

    private double getNextPrice(String symbol) {
        return lastPrices.compute(symbol, (k, v) -> {
            if (v == null) v = 150.0;
            double delta = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5;
            return Math.max(1.0, v + delta);
        });
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
