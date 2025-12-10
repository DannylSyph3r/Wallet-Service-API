package dev.slethware.walletservice.models.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "50.00", message = "Minimum withdrawal amount is 50.00")
    private BigDecimal amount;
}