package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuickAuthResponse {

    private String message;
    private Boolean success;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("mfa_required")
    private Boolean mfaRequired;

    @JsonProperty("session_token")
    private String sessionToken;

    @JsonProperty("mfa_secret")
    private String mfaSecretUri;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("cognito_sub")
    private String cognitoSub;
}
