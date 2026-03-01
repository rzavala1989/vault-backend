package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CitizenshipTaxRequest {

    @JsonProperty("id_type")
    private String idType;

    @JsonProperty("id_number")
    private String idNumber;

    @JsonProperty("country_code")
    private String countryCode;
}
