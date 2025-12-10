package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    @NotNull(message = "Amount is required")
    @Min(value = 10000, message = "Amount must be at least 10000 kobo (100 NGN)")
    private Long amount;
}