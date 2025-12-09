package dev.slethware.walletservice.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_wallet_number", columnList = "wallet_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class Wallet extends Auditable {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "wallet_number", unique = true, nullable = false, length = 10)
    private String walletNumber;

    @Column(nullable = false)
    private Long balance = 0L;

    public BigDecimal getBalanceInNaira() {
        return BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public void setBalanceFromNaira(BigDecimal naira) {
        this.balance = naira.multiply(BigDecimal.valueOf(100)).longValue();
    }
}