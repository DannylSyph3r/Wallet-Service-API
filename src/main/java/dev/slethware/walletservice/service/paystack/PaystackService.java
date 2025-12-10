package dev.slethware.walletservice.service.paystack;

public interface PaystackService {
    String initializeTransaction(String email, Long amountInKobo, String reference);
    boolean verifyWebhookSignature(String payload, String signature);
    String generateReference();
}