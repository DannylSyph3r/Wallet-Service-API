package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GoogleAuthRequest(
        @NotBlank(message = "ID token is required")
        String idToken
) {}