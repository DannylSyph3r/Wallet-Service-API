package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

@Builder
public record BalanceResponse(
        Long balance,
        String walletNumber
) {}