package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record DepositRequest(
        @NotNull(message = "Amount is required")
        @Min(value = 10000, message = "Amount must be at least 10000 kobo (100 NGN)")
        Long amount
) {}