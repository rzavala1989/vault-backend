package com.vault.service;

import com.vault.config.VaultProperties;
import com.vault.dto.QuickAuthRequest;
import com.vault.dto.QuickAuthResponse;
import com.vault.entity.RefreshToken;
import com.vault.entity.User;
import com.vault.entity.UserRole;
import com.vault.entity.UserStatus;
import com.vault.exception.ApiException;
import com.vault.repository.RefreshTokenRepository;
import com.vault.repository.UserRepository;
import com.vault.security.JwtProvider;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final VaultProperties vaultProperties;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(), new SystemTimeProvider());

    public QuickAuthResponse dispatch(QuickAuthRequest request) {
        return switch (request.getAction()) {
            case "register" -> register(request);
            case "confirm" -> confirm(request);
            case "resend" -> resend(request);
            case "login" -> login(request);
            case "login_with_mfa" -> loginWithMfa(request);
            case "forgot-password" -> forgotPassword(request);
            case "reset-password" -> resetPassword(request);
            case "oauth-exchange" -> oauthExchange(request);
            case "refresh" -> refresh(request);
            default -> throw ApiException.badRequest("INVALID_ACTION",
                    "Unknown action: " + request.getAction());
        };
    }

    @Transactional
    public QuickAuthResponse register(QuickAuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("EMAIL_EXISTS", "Email already registered");
        }

        String confirmationCode = generateCode();

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.PENDING_CONFIRMATION)
                .confirmationCode(confirmationCode)
                .build();

        userRepository.save(user);
        log.info("User registered: {} | confirmation code: {}", user.getEmail(), confirmationCode);

        return QuickAuthResponse.builder()
                .success(true)
                .message("User registered. Check logs for confirmation code.")
                .userId(user.getId().toString())
                .cognitoSub(user.getCognitoSub())
                .build();
    }

    @Transactional
    public QuickAuthResponse confirm(QuickAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            return QuickAuthResponse.builder().success(true).message("Already confirmed").build();
        }

        if (user.getConfirmationCode() == null || !user.getConfirmationCode().equals(request.getCode())) {
            throw ApiException.badRequest("INVALID_CODE", "Invalid confirmation code");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setConfirmationCode(null);
        userRepository.save(user);

        return QuickAuthResponse.builder()
                .success(true)
                .message("Email confirmed")
                .build();
    }

    public QuickAuthResponse resend(QuickAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        String code = generateCode();
        user.setConfirmationCode(code);
        userRepository.save(user);

        log.info("Resent confirmation code for {}: {}", user.getEmail(), code);

        return QuickAuthResponse.builder()
                .success(true)
                .message("Confirmation code resent. Check logs.")
                .build();
    }

    public QuickAuthResponse login(QuickAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("AUTH_FAILED", "Invalid credentials"));

        if (user.getStatus() == UserStatus.PENDING_CONFIRMATION) {
            throw ApiException.unauthorized("NOT_CONFIRMED", "Email not confirmed");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw ApiException.unauthorized("ACCOUNT_DISABLED", "Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("AUTH_FAILED", "Invalid credentials");
        }

        if (user.isMfaEnabled()) {
            String sessionToken = UUID.randomUUID().toString();
            return QuickAuthResponse.builder()
                    .mfaRequired(true)
                    .sessionToken(sessionToken)
                    .userId(user.getId().toString())
                    .build();
        }

        return issueTokens(user);
    }

    public QuickAuthResponse loginWithMfa(QuickAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("AUTH_FAILED", "Invalid credentials"));

        if (!user.isMfaEnabled() || user.getMfaSecret() == null) {
            throw ApiException.badRequest("MFA_NOT_ENABLED", "MFA is not enabled for this account");
        }

        if (!codeVerifier.isValidCode(user.getMfaSecret(), request.getMfaCode())) {
            throw ApiException.unauthorized("INVALID_MFA", "Invalid MFA code");
        }

        return issueTokens(user);
    }

    @Transactional
    public QuickAuthResponse forgotPassword(QuickAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        String resetCode = generateCode();
        user.setResetCode(resetCode);
        userRepository.save(user);

        log.info("Password reset code for {}: {}", user.getEmail(), resetCode);

        return QuickAuthResponse.builder()
                .success(true)
                .message("Reset code sent. Check logs.")
                .build();
    }

    @Transactional
    public QuickAuthResponse resetPassword(QuickAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        if (user.getResetCode() == null || !user.getResetCode().equals(request.getCode())) {
            throw ApiException.badRequest("INVALID_CODE", "Invalid reset code");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        userRepository.save(user);

        return QuickAuthResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .build();
    }

    public QuickAuthResponse oauthExchange(QuickAuthRequest request) {
        // Mock OAuth: create or find user by email derived from the oauth code
        String email = request.getEmail() != null ? request.getEmail() : request.getOauthCode() + "@oauth.mock";

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            return userRepository.save(newUser);
        });

        return issueTokens(user);
    }

    @Transactional
    public QuickAuthResponse refresh(QuickAuthRequest request) {
        if (request.getRefreshToken() == null) {
            throw ApiException.badRequest("MISSING_TOKEN", "Refresh token required");
        }

        RefreshToken rt = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> ApiException.unauthorized("INVALID_TOKEN", "Invalid refresh token"));

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw ApiException.unauthorized("TOKEN_EXPIRED", "Refresh token expired");
        }

        User user = rt.getUser();
        refreshTokenRepository.delete(rt);

        return issueTokens(user);
    }

    public QuickAuthResponse setupMfa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        String secret = secretGenerator.generate();
        user.setMfaSecret(secret);
        userRepository.save(user);

        String otpAuthUrl = String.format("otpauth://totp/Vault:%s?secret=%s&issuer=Vault", email, secret);

        return QuickAuthResponse.builder()
                .success(true)
                .mfaSecretUri(otpAuthUrl)
                .build();
    }

    @Transactional
    public QuickAuthResponse verifyMfaSetup(String email, String code, String secret) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        String mfaSecret = secret != null ? secret : user.getMfaSecret();
        if (mfaSecret == null) {
            throw ApiException.badRequest("MFA_NOT_SETUP", "MFA setup not initiated");
        }

        if (!codeVerifier.isValidCode(mfaSecret, code)) {
            throw ApiException.badRequest("INVALID_MFA", "Invalid MFA code");
        }

        user.setMfaSecret(mfaSecret);
        user.setMfaEnabled(true);
        userRepository.save(user);

        return QuickAuthResponse.builder()
                .success(true)
                .message("MFA enabled successfully")
                .build();
    }

    private QuickAuthResponse issueTokens(User user) {
        String idToken = jwtProvider.generateIdToken(
                user.getCognitoSub(), user.getEmail(), user.getRole().name());
        String accessToken = jwtProvider.generateAccessToken(user.getCognitoSub());
        String refreshToken = createRefreshToken(user);

        return QuickAuthResponse.builder()
                .idToken(idToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId().toString())
                .cognitoSub(user.getCognitoSub())
                .build();
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken rt = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusMillis(vaultProperties.getJwt().getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(rt);
        return token;
    }

    private String generateCode() {
        return String.format("%06d", (int) (Math.random() * 1_000_000));
    }
}
