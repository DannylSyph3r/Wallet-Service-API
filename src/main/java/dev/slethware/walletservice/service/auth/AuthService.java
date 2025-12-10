package dev.slethware.walletservice.service.auth;

import dev.slethware.walletservice.models.dtos.request.GoogleAuthRequest;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.dtos.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthService {
    ApiResponse<AuthResponse> googleAuth(GoogleAuthRequest request);
    void initiateGoogleOAuth(HttpServletResponse response) throws IOException;
    ApiResponse<AuthResponse> googleCallback(String code);
}