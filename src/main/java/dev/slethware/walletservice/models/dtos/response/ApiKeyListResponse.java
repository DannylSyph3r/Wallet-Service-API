package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ApiKeyListResponse(
        UUID id,
        String name,
        List<String> permissions,
        LocalDateTime expiresAt,
        boolean revoked,
        LocalDateTime createdAt
) {}