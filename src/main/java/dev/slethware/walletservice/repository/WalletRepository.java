package dev.slethware.walletservice.repository;

import dev.slethware.walletservice.models.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByWalletNumber(String walletNumber);
    boolean existsByWalletNumber(String walletNumber);
}