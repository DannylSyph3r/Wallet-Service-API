package dev.slethware.walletservice.service.paystack;

import java.math.BigDecimal;
import java.util.Map;

public interface PaystackService {
    String initializeTransaction(String email, BigDecimal amount, String reference);
    boolean verifyWebhookSignature(Map<String, Object> payload, String signature);
    String generateReference();
}