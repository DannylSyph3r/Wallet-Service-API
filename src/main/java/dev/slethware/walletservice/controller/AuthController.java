package dev.slethware.walletservice.controller;

import dev.slethware.walletservice.models.dtos.request.GoogleAuthRequest;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.dtos.response.AuthResponse;
import dev.slethware.walletservice.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/google")
    @Operation(
            summary = "Initiate Google Sign-In",
            description = "Redirects to Google OAuth consent screen to begin authentication flow."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Redirect to Google OAuth")
    })
    public void initiateGoogleAuth(HttpServletResponse response) throws IOException {
        authService.initiateGoogleOAuth(response);
    }

    @GetMapping("/google/callback")
    @Operation(
            summary = "Google Sign-In Callback",
            description = "Handles the OAuth callback from Google. Creates user if not existing and returns JWT token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid authorization code")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> googleCallback(
            @Parameter(description = "Authorization code from Google") @RequestParam String code) {
        return ResponseEntity.ok(authService.googleCallback(code));
    }

    @PostMapping("/google")
    @Operation(
            summary = "Google Authentication (ID Token)",
            description = "Authenticate user with Google ID token. Creates user and wallet if new. Alternative to traditional OAuth flow."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid Google token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.googleAuth(request));
    }
}