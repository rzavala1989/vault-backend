package com.vault.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.vault.service.AlpacaProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/a")
@RequiredArgsConstructor
public class AccountController {

    private final AlpacaProxyService alpacaProxy;

    // ── Health ──

    @GetMapping("/get-health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "accounts"));
    }

    @GetMapping("/get-account-startup-data")
    public ResponseEntity<Map<String, Object>> getStartupData() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "market_open", true,
                "server_time", java.time.Instant.now().toString()
        ));
    }

    // ── Accounts ──

    @GetMapping("/get-accounts")
    public ResponseEntity<JsonNode> getAccounts() {
        return ResponseEntity.ok(alpacaProxy.getAccounts());
    }

    @PostMapping("/create-account")
    public ResponseEntity<JsonNode> createAccount(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alpacaProxy.createAccount(body));
    }

    @GetMapping("/get-trading-details-for-account")
    public ResponseEntity<JsonNode> getTradingDetails(@RequestParam("account") String accountId) {
        return ResponseEntity.ok(alpacaProxy.getTradingDetails(accountId));
    }

    // ── Assets ──

    @GetMapping("/get-assets")
    public ResponseEntity<JsonNode> getAssets(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "asset_class", required = false) String assetClass) {
        return ResponseEntity.ok(alpacaProxy.getAssets(status, assetClass));
    }

    // ── Positions ──

    @GetMapping("/get-open-positions-for-account-symbol")
    public ResponseEntity<JsonNode> getPositions(
            @RequestParam("account") String accountId,
            @RequestParam(value = "symbol", required = false) String symbol) {
        if (symbol != null) {
            return ResponseEntity.ok(alpacaProxy.getPosition(accountId, symbol));
        }
        return ResponseEntity.ok(alpacaProxy.getPositions(accountId));
    }

    @GetMapping("/get-eod-positions")
    public ResponseEntity<JsonNode> getEodPositions(@RequestParam("account") String accountId) {
        return ResponseEntity.ok(alpacaProxy.getPositions(accountId));
    }

    @GetMapping("/get-eod-cash-details")
    public ResponseEntity<JsonNode> getEodCashDetails(@RequestParam("account") String accountId) {
        return ResponseEntity.ok(alpacaProxy.getTradingDetails(accountId));
    }

    // ── Orders ──

    @GetMapping("/get-orders-for-account")
    public ResponseEntity<JsonNode> getOrders(
            @RequestParam("account") String accountId,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(alpacaProxy.getOrders(accountId, status));
    }

    @PostMapping("/create-order-for-account")
    public ResponseEntity<JsonNode> createOrder(
            @RequestParam("account") String accountId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alpacaProxy.createOrder(accountId, body));
    }

    @PostMapping("/estimate-order-for-account")
    public ResponseEntity<JsonNode> estimateOrder(
            @RequestParam("account") String accountId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alpacaProxy.estimateOrder(accountId, body));
    }

    @PatchMapping("/replace-order-for-account-order-id")
    public ResponseEntity<JsonNode> replaceOrder(
            @RequestParam("account") String accountId,
            @RequestParam("order_id") String orderId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alpacaProxy.replaceOrder(accountId, orderId, body));
    }

    @DeleteMapping("/cancel-order-for-account-order-id")
    public ResponseEntity<JsonNode> cancelOrder(
            @RequestParam("account") String accountId,
            @RequestParam("order_id") String orderId) {
        return ResponseEntity.ok(alpacaProxy.cancelOrder(accountId, orderId));
    }

    // ── ACH Relationships ──

    @GetMapping("/get-ach_relationships-for-account")
    public ResponseEntity<JsonNode> getAchRelationships(@RequestParam("account") String accountId) {
        return ResponseEntity.ok(alpacaProxy.getAchRelationships(accountId));
    }

    @PostMapping("/create-ach-relationship-for-account")
    public ResponseEntity<JsonNode> createAchRelationship(
            @RequestParam("account") String accountId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alpacaProxy.createAchRelationship(accountId, body));
    }

    @DeleteMapping("/delete-ach-relationship-for-account")
    public ResponseEntity<JsonNode> deleteAchRelationship(
            @RequestParam("account") String accountId,
            @RequestParam("relationship_id") String relationshipId) {
        return ResponseEntity.ok(alpacaProxy.deleteAchRelationship(accountId, relationshipId));
    }

    // ── Transfers ──

    @GetMapping("/get-transfers-for-account")
    public ResponseEntity<JsonNode> getTransfers(@RequestParam("account") String accountId) {
        return ResponseEntity.ok(alpacaProxy.getTransfers(accountId));
    }

    @PostMapping("/request-new-transfer-for-account")
    public ResponseEntity<JsonNode> createTransfer(
            @RequestParam("account") String accountId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(alpacaProxy.createTransfer(accountId, body));
    }

    // ── Market Screeners ──

    @GetMapping("/get-top-market-movers")
    public ResponseEntity<JsonNode> getTopMarketMovers(
            @RequestParam(value = "market_type", required = false) String marketType) {
        return ResponseEntity.ok(alpacaProxy.getTopMarketMovers(marketType));
    }

    @GetMapping("/get-most-active-stocks")
    public ResponseEntity<JsonNode> getMostActiveStocks() {
        return ResponseEntity.ok(alpacaProxy.getMostActiveStocks());
    }
}
