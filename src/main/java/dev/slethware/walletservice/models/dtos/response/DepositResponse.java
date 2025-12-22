package dev.slethware.walletservice.models.dtos.response;

import lombok.Builder;

@Builder
public record DepositResponse(
        String reference,
        String authorizationUrl
) {}