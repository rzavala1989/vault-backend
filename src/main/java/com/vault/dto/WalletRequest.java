package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WalletRequest {

    private String address;
    private String chain;

    @JsonProperty("wallet_id")
    private Long walletId;
}
