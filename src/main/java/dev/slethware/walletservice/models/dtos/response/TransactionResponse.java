package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        String type,
        Long amount,
        String status,
        String reference,
        LocalDateTime createdAt
) {}