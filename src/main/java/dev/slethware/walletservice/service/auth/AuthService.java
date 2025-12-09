package dev.slethware.walletservice.service.auth;

import dev.slethware.walletservice.models.dtos.request.GoogleAuthRequest;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.dtos.response.AuthResponse;

public interface AuthService {
    ApiResponse<AuthResponse> googleAuth(GoogleAuthRequest request);
}