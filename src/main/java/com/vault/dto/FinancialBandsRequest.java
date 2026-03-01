package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinancialBandsRequest {

    @JsonProperty("annual_income_min")
    private String annualIncomeMin;

    @JsonProperty("annual_income_max")
    private String annualIncomeMax;

    @JsonProperty("liquid_net_worth_min")
    private String liquidNetWorthMin;

    @JsonProperty("liquid_net_worth_max")
    private String liquidNetWorthMax;

    @JsonProperty("total_net_worth_min")
    private String totalNetWorthMin;

    @JsonProperty("total_net_worth_max")
    private String totalNetWorthMax;

    @JsonProperty("funding_sources")
    private String fundingSources;
}
