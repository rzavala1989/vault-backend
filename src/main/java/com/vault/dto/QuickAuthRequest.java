package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QuickAuthRequest {

    private String action;
    private String email;
    private String password;
    private String code;

    @JsonProperty("new_password")
    private String newPassword;

    @JsonProperty("mfa_code")
    private String mfaCode;

    @JsonProperty("mfa_secret")
    private String mfaSecret;

    @JsonProperty("session_token")
    private String sessionToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("oauth_provider")
    private String oauthProvider;

    @JsonProperty("oauth_code")
    private String oauthCode;
}
