package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateApiKeyRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotEmpty(message = "Permissions are required")
        List<String> permissions,

        @NotBlank(message = "Expiry is required")
        @Pattern(regexp = "^(1H|1D|1M|1Y)$", message = "Expiry must be one of: 1H, 1D, 1M, 1Y")
        String expiry
) {}