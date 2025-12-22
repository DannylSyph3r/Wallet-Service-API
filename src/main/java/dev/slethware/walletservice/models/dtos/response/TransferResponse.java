package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

@Builder
public record TransferResponse(
        String reference,
        String status,
        String message
) {}