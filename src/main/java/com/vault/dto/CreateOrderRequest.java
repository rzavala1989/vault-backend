package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateOrderRequest {

    private String symbol;
    private String qty;
    private String notional;
    private String side;
    private String type;

    @JsonProperty("time_in_force")
    private String timeInForce;

    @JsonProperty("limit_price")
    private String limitPrice;

    @JsonProperty("stop_price")
    private String stopPrice;

    @JsonProperty("extended_hours")
    private Boolean extendedHours;
}
