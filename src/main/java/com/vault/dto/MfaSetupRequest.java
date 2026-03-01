package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MfaSetupRequest {

    @JsonProperty("mfa_code")
    private String mfaCode;

    @JsonProperty("mfa_secret")
    private String mfaSecret;

    private String email;

    @JsonProperty("session_token")
    private String sessionToken;
}
