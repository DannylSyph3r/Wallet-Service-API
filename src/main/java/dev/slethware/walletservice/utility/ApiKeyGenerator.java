package dev.slethware.walletservice.utility;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int KEY_LENGTH = 32;

    public static String generate() {
        byte[] bytes = new byte[KEY_LENGTH];
        random.nextBytes(bytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "sk_live_" + randomPart;
    }
}