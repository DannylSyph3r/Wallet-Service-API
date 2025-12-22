package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TransferRequest(
        @NotBlank(message = "Wallet number is required")
        @Size(min = 10, max = 10, message = "Wallet number must be exactly 10 digits")
        String walletNumber,

        @NotNull(message = "Amount is required")
        @Min(value = 100, message = "Amount must be at least 100 kobo (1 NGN)")
        Long amount
) {}