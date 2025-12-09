package dev.slethware.walletservice.repository;

import dev.slethware.walletservice.models.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByUserIdAndRevokedFalse(UUID userId);
    long countByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, java.time.LocalDateTime now);
}