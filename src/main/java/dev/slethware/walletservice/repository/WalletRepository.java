package dev.slethware.walletservice.repository;

import dev.slethware.walletservice.models.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByWalletNumber(String walletNumber);
    boolean existsByWalletNumber(String walletNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletNumber = :walletNumber")
    Optional<Wallet> findByWalletNumberForUpdate(@Param("walletNumber") String walletNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);
}