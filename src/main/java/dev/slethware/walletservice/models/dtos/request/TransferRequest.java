package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotBlank(message = "Wallet number is required")
    @Size(min = 10, max = 10, message = "Wallet number must be exactly 10 digits")
    private String walletNumber;

    @NotNull(message = "Amount is required")
    @Min(value = 100, message = "Amount must be at least 100 kobo (1 NGN)")
    private Long amount;
}