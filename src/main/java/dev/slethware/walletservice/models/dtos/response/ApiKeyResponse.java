package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApiKeyResponse(
        String apiKey,
        LocalDateTime expiresAt
) {}