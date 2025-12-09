package dev.slethware.walletservice.service.paystack;

import com.google.gson.Gson;
import dev.slethware.walletservice.exception.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Slf4j
@Service
public class PaystackServiceImpl implements PaystackService {

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    @Value("${paystack.api-url}")
    private String paystackApiUrl;

    private final WebClient webClient;
    private final Gson gson;

    public PaystackServiceImpl(WebClient.Builder webClientBuilder, Gson gson) {
        this.webClient = webClientBuilder.build();
        this.gson = gson;
    }

    @Override
    public String initializeTransaction(String email, BigDecimal amount, String reference) {
        long amountInKobo = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "amount", amountInKobo,
                "reference", reference
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri(paystackApiUrl + "/transaction/initialize")
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("authorization_url");
            }

            throw new InternalServerException("Failed to initialize Paystack transaction");
        } catch (Exception e) {
            log.error("Paystack initialization failed: {}", e.getMessage());
            throw new InternalServerException("Failed to initialize Paystack transaction", e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, Object> payload, String signature) {
        SecretKeySpec key = new SecretKeySpec(paystackSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA512");
            mac.init(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new InternalServerException("Failed to verify Paystack webhook", e);
        }

        byte[] bytes = mac.doFinal(gson.toJson(payload).getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }

        return result.toString().equals(signature);
    }

    @Override
    public String generateReference() {
        return "WLLT_" + System.currentTimeMillis();
    }
}