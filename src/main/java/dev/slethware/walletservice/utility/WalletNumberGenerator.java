package dev.slethware.walletservice.utility;

import java.security.SecureRandom;

public class WalletNumberGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        long number = 1000000000L + random.nextLong(9000000000L);
        return String.valueOf(number);
    }
}