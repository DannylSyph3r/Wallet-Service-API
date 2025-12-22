package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

@Builder
public record WithdrawResponse(
        String reference,
        String status,
        String message
) {}