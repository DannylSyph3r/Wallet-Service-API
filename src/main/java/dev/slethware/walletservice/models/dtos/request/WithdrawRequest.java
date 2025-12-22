package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WithdrawRequest(
        @NotNull(message = "Amount is required")
        @Min(value = 5000, message = "Minimum withdrawal amount is 5000 kobo (50 NGN)")
        Long amount
) {}