package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record RolloverApiKeyRequest(
        @NotBlank(message = "Expired key ID is required")
        String expiredKeyId,

        @NotBlank(message = "Expiry is required")
        @Pattern(regexp = "^(1H|1D|1M|1Y)$", message = "Expiry must be one of: 1H, 1D, 1M, 1Y")
        String expiry
) {}