package com.vault.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PersonalInfoRequest {

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    private String phone;
    private String nationality;

    @JsonProperty("tax_id")
    private String taxId;

    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
}
