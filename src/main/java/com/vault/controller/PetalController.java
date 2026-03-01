package com.vault.controller;

import com.vault.dto.UserSettingsRequest;
import com.vault.dto.WalletRequest;
import com.vault.entity.UserApiKey;
import com.vault.entity.UserSetting;
import com.vault.exception.ApiException;
import com.vault.repository.UserApiKeyRepository;
import com.vault.repository.UserRepository;
import com.vault.repository.UserSettingRepository;
import com.vault.security.UserPrincipal;
import com.vault.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/p")
@RequiredArgsConstructor
public class PetalController {

    private final UserSettingRepository settingRepository;
    private final UserApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    @GetMapping("/get-health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "petal"));
    }

    // ── Settings ──

    @GetMapping("/get-user-settings")
    public ResponseEntity<Map<String, String>> getUserSettings(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<UserSetting> settings = settingRepository.findByUserId(principal.getId());
        Map<String, String> map = settings.stream()
                .collect(Collectors.toMap(UserSetting::getSettingName, UserSetting::getSettingValue));
        return ResponseEntity.ok(map);
    }

    @PostMapping("/save-user-settings")
    public ResponseEntity<Map<String, Object>> saveUserSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UserSettingsRequest request) {
        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        request.getSettings().forEach((name, value) -> {
            UserSetting setting = settingRepository
                    .findByUserIdAndSettingName(principal.getId(), name)
                    .orElse(UserSetting.builder().user(user).settingName(name).build());
            setting.setSettingValue(value);
            settingRepository.save(setting);
        });

        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── API Keys ──

    @GetMapping("/get-api-key")
    public ResponseEntity<?> getApiKey(@AuthenticationPrincipal UserPrincipal principal) {
        List<UserApiKey> keys = apiKeyRepository.findByUserId(principal.getId());
        if (keys.isEmpty()) {
            return ResponseEntity.ok(Map.of("api_key", (Object) null));
        }
        UserApiKey latest = keys.get(keys.size() - 1);
        return ResponseEntity.ok(Map.of(
                "api_key", latest.getApiKey(),
                "created_at", latest.getCreatedAt().toString()
        ));
    }

    @PostMapping("/create-api-key")
    public ResponseEntity<Map<String, Object>> createApiKey(
            @AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        String key = "pk_" + UUID.randomUUID().toString().replace("-", "");

        UserApiKey apiKey = UserApiKey.builder()
                .user(user)
                .apiKey(key)
                .build();

        apiKeyRepository.save(apiKey);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "api_key", key
        ));
    }

    // ── Wallets ──

    @GetMapping("/get-crypto-funding-wallets")
    public ResponseEntity<?> getCryptoFundingWallets(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(walletService.getCryptoFundingWallets(principal.getId()));
    }

    @GetMapping("/get-crypto-funding-transfers-for-acct")
    public ResponseEntity<?> getCryptoFundingTransfers(
            @AuthenticationPrincipal UserPrincipal principal) {
        // Mock: return empty transfers list
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/get-user-wallets")
    public ResponseEntity<?> getUserWallets(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(walletService.getUserWallets(principal.getId()));
    }

    @PostMapping("/save-wallet-address")
    public ResponseEntity<Map<String, Object>> saveWalletAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody WalletRequest request) {
        return ResponseEntity.ok(
                walletService.saveWalletAddress(principal.getId(), request.getAddress(), request.getChain()));
    }

    @PostMapping("/set-primary-wallet")
    public ResponseEntity<Map<String, Object>> setPrimaryWallet(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody WalletRequest request) {
        return ResponseEntity.ok(
                walletService.setPrimaryWallet(principal.getId(), request.getWalletId()));
    }
}
