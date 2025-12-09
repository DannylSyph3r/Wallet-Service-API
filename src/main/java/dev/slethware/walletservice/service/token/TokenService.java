package dev.slethware.walletservice.service.token;

import dev.slethware.walletservice.models.entity.User;
import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateAccessToken(Authentication authentication, User user);
    String extractEmail(String token);
    boolean validateAccessToken(String token, String email);
}