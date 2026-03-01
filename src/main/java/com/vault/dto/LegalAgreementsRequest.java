package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LegalAgreementsRequest {

    private List<Agreement> agreements;

    @Data
    public static class Agreement {

        @JsonProperty("agreement_type")
        private String agreementType;

        private String revision;
    }
}
