package com.vault.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserSettingsRequest {

    private Map<String, String> settings;
}
