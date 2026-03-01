package com.vault.controller;

import com.vault.dto.MfaSetupRequest;
import com.vault.dto.QuickAuthRequest;
import com.vault.dto.QuickAuthResponse;
import com.vault.dto.UpdateUserRequest;
import com.vault.dto.UserResponse;
import com.vault.entity.User;
import com.vault.exception.ApiException;
import com.vault.repository.UserRepository;
import com.vault.security.UserPrincipal;
import com.vault.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/u/quick-auth")
    public ResponseEntity<QuickAuthResponse> quickAuth(@RequestBody QuickAuthRequest request) {
        return ResponseEntity.ok(authService.dispatch(request));
    }

    @PostMapping("/verify-mfa-setup")
    public ResponseEntity<QuickAuthResponse> verifyMfaSetup(@RequestBody MfaSetupRequest request) {
        return ResponseEntity.ok(
                authService.verifyMfaSetup(request.getEmail(), request.getMfaCode(), request.getMfaSecret()));
    }

    @GetMapping("/u/get-user")
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = findUser(principal);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PutMapping("/u/update-user")
    public ResponseEntity<UserResponse> updateUser(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestBody UpdateUserRequest request) {
        User user = findUser(principal);

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PostMapping("/u/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        User user = findUser(principal);
        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted"));
    }

    @PostMapping("/u/setup-mfa")
    public ResponseEntity<QuickAuthResponse> setupMfa(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.setupMfa(principal.getEmail()));
    }

    private User findUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .cognitoSub(user.getCognitoSub())
                .mfaEnabled(user.isMfaEnabled())
                .onboardingStatus(user.getOnboardingStatus().name())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}
