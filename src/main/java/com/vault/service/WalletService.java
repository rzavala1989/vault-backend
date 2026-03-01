package com.vault.service;

import com.vault.entity.User;
import com.vault.entity.UserWallet;
import com.vault.exception.ApiException;
import com.vault.repository.UserRepository;
import com.vault.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserWalletRepository walletRepository;
    private final UserRepository userRepository;

    public List<UserWallet> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    @Transactional
    public Map<String, Object> saveWalletAddress(Long userId, String address, String chain) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        UserWallet wallet = UserWallet.builder()
                .user(user)
                .address(address)
                .chain(chain)
                .primary(walletRepository.findByUserId(userId).isEmpty())
                .build();

        walletRepository.save(wallet);
        return Map.of("success", true, "wallet_id", wallet.getId());
    }

    @Transactional
    public Map<String, Object> setPrimaryWallet(Long userId, Long walletId) {
        List<UserWallet> wallets = walletRepository.findByUserId(userId);

        for (UserWallet w : wallets) {
            w.setPrimary(w.getId().equals(walletId));
        }

        walletRepository.saveAll(wallets);
        return Map.of("success", true);
    }

    public List<Map<String, Object>> getCryptoFundingWallets(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(w -> Map.<String, Object>of(
                        "id", w.getId(),
                        "address", w.getAddress(),
                        "chain", w.getChain() != null ? w.getChain() : "",
                        "is_primary", w.isPrimary()
                ))
                .toList();
    }
}
