package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token
) {}