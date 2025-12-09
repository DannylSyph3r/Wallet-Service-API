package dev.slethware.walletservice.models.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String type;
    private BigDecimal amount;
    private String status;
    private String reference;
    private LocalDateTime createdAt;
}