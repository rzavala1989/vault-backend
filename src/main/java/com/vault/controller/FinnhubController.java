package com.vault.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.vault.service.FinnhubProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/f")
@RequiredArgsConstructor
public class FinnhubController {

    private final FinnhubProxyService finnhubProxy;

    @GetMapping("/get-company-profile")
    public ResponseEntity<JsonNode> getCompanyProfile(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(finnhubProxy.getCompanyProfile(symbol));
    }

    @GetMapping("/get-basic-financials")
    public ResponseEntity<JsonNode> getBasicFinancials(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "metric", required = false) String metric) {
        return ResponseEntity.ok(finnhubProxy.getBasicFinancials(symbol, metric));
    }

    @GetMapping("/get-financials")
    public ResponseEntity<JsonNode> getFinancials(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "statement", required = false) String statement,
            @RequestParam(value = "freq", required = false) String freq) {
        return ResponseEntity.ok(finnhubProxy.getFinancials(symbol, statement, freq));
    }

    @GetMapping("/get-market-cap")
    public ResponseEntity<JsonNode> getMarketCap(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(finnhubProxy.getMarketCap(symbol));
    }

    @GetMapping("/get-dividends")
    public ResponseEntity<JsonNode> getDividends(
            @RequestParam("symbol") String symbol,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        return ResponseEntity.ok(finnhubProxy.getDividends(symbol, from, to));
    }

    @GetMapping("/get-market-news")
    public ResponseEntity<JsonNode> getMarketNews(
            @RequestParam(value = "category", required = false) String category) {
        return ResponseEntity.ok(finnhubProxy.getMarketNews(category));
    }

    @GetMapping("/get-company-news")
    public ResponseEntity<JsonNode> getCompanyNews(
            @RequestParam("symbol") String symbol,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        return ResponseEntity.ok(finnhubProxy.getCompanyNews(symbol, from, to));
    }

    @GetMapping("/get-press-releases")
    public ResponseEntity<JsonNode> getPressReleases(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(finnhubProxy.getPressReleases(symbol));
    }

    @GetMapping("/get-revenue-estimates")
    public ResponseEntity<JsonNode> getRevenueEstimates(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "freq", required = false) String freq) {
        return ResponseEntity.ok(finnhubProxy.getRevenueEstimates(symbol, freq));
    }

    @GetMapping("/get-eps-estimates")
    public ResponseEntity<JsonNode> getEpsEstimates(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "freq", required = false) String freq) {
        return ResponseEntity.ok(finnhubProxy.getEpsEstimates(symbol, freq));
    }

    @GetMapping("/get-ebitda-estimates")
    public ResponseEntity<JsonNode> getEbitdaEstimates(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "freq", required = false) String freq) {
        return ResponseEntity.ok(finnhubProxy.getEbitdaEstimates(symbol, freq));
    }

    @GetMapping("/get-ebit-estimates")
    public ResponseEntity<JsonNode> getEbitEstimates(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "freq", required = false) String freq) {
        return ResponseEntity.ok(finnhubProxy.getEbitEstimates(symbol, freq));
    }

    @GetMapping("/get-price-target")
    public ResponseEntity<JsonNode> getPriceTarget(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(finnhubProxy.getPriceTarget(symbol));
    }

    @GetMapping("/get-upgrade-downgrade")
    public ResponseEntity<JsonNode> getUpgradeDowngrade(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(finnhubProxy.getUpgradeDowngrade(symbol));
    }
}
