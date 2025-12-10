package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    @NotNull(message = "Amount is required")
    @Min(value = 5000, message = "Minimum withdrawal amount is 5000 kobo (50 NGN)")
    private Long amount;
}