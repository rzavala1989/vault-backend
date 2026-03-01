package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmploymentRequest {

    @JsonProperty("employment_status")
    private String employmentStatus;

    private String employer;
    private String position;

    @JsonProperty("employer_address")
    private String employerAddress;
}
