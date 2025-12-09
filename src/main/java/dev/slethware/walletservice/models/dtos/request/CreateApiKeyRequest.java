package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiKeyRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotEmpty(message = "Permissions are required")
    private List<String> permissions;

    @NotBlank(message = "Expiry is required")
    @Pattern(regexp = "^(1H|1D|1M|1Y)$", message = "Expiry must be one of: 1H, 1D, 1M, 1Y")
    private String expiry;
}