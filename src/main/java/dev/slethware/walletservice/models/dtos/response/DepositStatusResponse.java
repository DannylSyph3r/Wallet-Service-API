package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

@Builder
public record DepositStatusResponse(
        String reference,
        String status,
        Long amount
) {}