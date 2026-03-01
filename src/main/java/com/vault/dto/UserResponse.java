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
public class UserResponse {

    private Long id;
    private String email;
    private String role;
    private String status;

    @JsonProperty("cognito_sub")
    private String cognitoSub;

    @JsonProperty("mfa_enabled")
    private Boolean mfaEnabled;

    @JsonProperty("onboarding_status")
    private String onboardingStatus;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String phone;

    @JsonProperty("created_at")
    private String createdAt;
}
