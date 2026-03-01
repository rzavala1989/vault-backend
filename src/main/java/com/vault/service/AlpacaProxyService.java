package com.vault.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class AlpacaProxyService {

    private final WebClient brokerClient;
    private final WebClient marketsClient;

    public AlpacaProxyService(@Qualifier("alpacaBrokerClient") WebClient brokerClient,
                               @Qualifier("alpacaMarketsClient") WebClient marketsClient) {
        this.brokerClient = brokerClient;
        this.marketsClient = marketsClient;
    }

    // ── Accounts ──

    public JsonNode getAccounts() {
        return brokerGet("/v1/accounts");
    }

    public JsonNode getAccount(String accountId) {
        return brokerGet("/v1/accounts/" + accountId);
    }

    public JsonNode createAccount(Map<String, Object> body) {
        return brokerPost("/v1/accounts", body);
    }

    public JsonNode getTradingDetails(String accountId) {
        return brokerGet("/v1/trading/accounts/" + accountId + "/account");
    }

    // ── Assets ──

    public JsonNode getAssets(String status, String assetClass) {
        String path = "/v1/assets?status=" + (status != null ? status : "active");
        if (assetClass != null) {
            path += "&asset_class=" + assetClass;
        }
        return brokerGet(path);
    }

    // ── Positions ──

    public JsonNode getPositions(String accountId) {
        return brokerGet("/v1/trading/accounts/" + accountId + "/positions");
    }

    public JsonNode getPosition(String accountId, String symbol) {
        return brokerGet("/v1/trading/accounts/" + accountId + "/positions/" + symbol);
    }

    // ── Orders ──

    public JsonNode getOrders(String accountId, String status) {
        String path = "/v1/trading/accounts/" + accountId + "/orders";
        if (status != null) {
            path += "?status=" + status;
        }
        return brokerGet(path);
    }

    public JsonNode createOrder(String accountId, Map<String, Object> body) {
        return brokerPost("/v1/trading/accounts/" + accountId + "/orders", body);
    }

    public JsonNode estimateOrder(String accountId, Map<String, Object> body) {
        return brokerPost("/v1/trading/accounts/" + accountId + "/orders/estimation", body);
    }

    public JsonNode replaceOrder(String accountId, String orderId, Map<String, Object> body) {
        return brokerPatch("/v1/trading/accounts/" + accountId + "/orders/" + orderId, body);
    }

    public JsonNode cancelOrder(String accountId, String orderId) {
        return brokerDelete("/v1/trading/accounts/" + accountId + "/orders/" + orderId);
    }

    // ── ACH Relationships ──

    public JsonNode getAchRelationships(String accountId) {
        return brokerGet("/v1/accounts/" + accountId + "/ach_relationships");
    }

    public JsonNode createAchRelationship(String accountId, Map<String, Object> body) {
        return brokerPost("/v1/accounts/" + accountId + "/ach_relationships", body);
    }

    public JsonNode deleteAchRelationship(String accountId, String relationshipId) {
        return brokerDelete("/v1/accounts/" + accountId + "/ach_relationships/" + relationshipId);
    }

    // ── Transfers ──

    public JsonNode getTransfers(String accountId) {
        return brokerGet("/v1/accounts/" + accountId + "/transfers");
    }

    public JsonNode createTransfer(String accountId, Map<String, Object> body) {
        return brokerPost("/v1/accounts/" + accountId + "/transfers", body);
    }

    // ── Market Data ──

    public JsonNode getTopMarketMovers(String marketType) {
        String type = marketType != null ? marketType : "stocks";
        return marketsGet("/v1beta1/screener/" + type + "/movers?top=20");
    }

    public JsonNode getMostActiveStocks() {
        return marketsGet("/v1beta1/screener/stocks/most-actives?top=20");
    }

    // ── Internal HTTP helpers ──

    private JsonNode brokerGet(String path) {
        return brokerClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, this::handleError)
                .block();
    }

    private JsonNode brokerPost(String path, Object body) {
        return brokerClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, this::handleError)
                .block();
    }

    private JsonNode brokerPatch(String path, Object body) {
        return brokerClient.patch()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, this::handleError)
                .block();
    }

    private JsonNode brokerDelete(String path) {
        return brokerClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, this::handleError)
                .block();
    }

    private JsonNode marketsGet(String path) {
        return marketsClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, this::handleError)
                .block();
    }

    private Mono<JsonNode> handleError(WebClientResponseException ex) {
        log.error("Alpaca API error [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return Mono.error(new com.vault.exception.ApiException(
                "ALPACA_ERROR",
                ex.getResponseBodyAsString(),
                org.springframework.http.HttpStatus.valueOf(ex.getStatusCode().value())));
    }
}
