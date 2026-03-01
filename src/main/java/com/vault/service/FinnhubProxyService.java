package com.vault.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vault.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class FinnhubProxyService {

    private final WebClient client;

    public FinnhubProxyService(@Qualifier("finnhubClient") WebClient client) {
        this.client = client;
    }

    public JsonNode getCompanyProfile(String symbol) {
        return get("/stock/profile2?symbol=" + symbol);
    }

    public JsonNode getBasicFinancials(String symbol, String metric) {
        return get("/stock/metric?symbol=" + symbol + "&metric=" + (metric != null ? metric : "all"));
    }

    public JsonNode getFinancials(String symbol, String statement, String freq) {
        return get("/stock/financials-reported?symbol=" + symbol
                + "&statement=" + (statement != null ? statement : "bs")
                + "&freq=" + (freq != null ? freq : "annual"));
    }

    public JsonNode getMarketCap(String symbol) {
        return get("/stock/metric?symbol=" + symbol + "&metric=all");
    }

    public JsonNode getDividends(String symbol, String from, String to) {
        return get("/stock/dividend?symbol=" + symbol + "&from=" + from + "&to=" + to);
    }

    public JsonNode getMarketNews(String category) {
        return get("/news?category=" + (category != null ? category : "general"));
    }

    public JsonNode getCompanyNews(String symbol, String from, String to) {
        return get("/company-news?symbol=" + symbol + "&from=" + from + "&to=" + to);
    }

    public JsonNode getPressReleases(String symbol) {
        return get("/press-releases?symbol=" + symbol);
    }

    public JsonNode getRevenueEstimates(String symbol, String freq) {
        return get("/stock/revenue-estimate?symbol=" + symbol + "&freq=" + (freq != null ? freq : "annual"));
    }

    public JsonNode getEpsEstimates(String symbol, String freq) {
        return get("/stock/eps-estimate?symbol=" + symbol + "&freq=" + (freq != null ? freq : "annual"));
    }

    public JsonNode getEbitdaEstimates(String symbol, String freq) {
        return get("/stock/ebitda-estimate?symbol=" + symbol + "&freq=" + (freq != null ? freq : "annual"));
    }

    public JsonNode getEbitEstimates(String symbol, String freq) {
        return get("/stock/ebit-estimate?symbol=" + symbol + "&freq=" + (freq != null ? freq : "annual"));
    }

    public JsonNode getPriceTarget(String symbol) {
        return get("/stock/price-target?symbol=" + symbol);
    }

    public JsonNode getUpgradeDowngrade(String symbol) {
        return get("/stock/upgrade-downgrade?symbol=" + symbol);
    }

    private JsonNode get(String path) {
        return client.get()
                .uri(path)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Finnhub API error [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.error(new ApiException(
                            "FINNHUB_ERROR",
                            ex.getResponseBodyAsString(),
                            org.springframework.http.HttpStatus.valueOf(ex.getStatusCode().value())));
                })
                .block();
    }
}
