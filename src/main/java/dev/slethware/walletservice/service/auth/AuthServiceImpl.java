package dev.slethware.walletservice.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dev.slethware.walletservice.exception.BadRequestException;
import dev.slethware.walletservice.models.dtos.request.GoogleAuthRequest;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.dtos.response.AuthResponse;
import dev.slethware.walletservice.models.entity.User;
import dev.slethware.walletservice.repository.UserRepository;
import dev.slethware.walletservice.service.token.TokenService;
import dev.slethware.walletservice.service.wallet.WalletService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final NetHttpTransport TRANSPORT = new NetHttpTransport();
    private static final GsonFactory JSON_FACTORY = new GsonFactory();

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WalletService walletService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri:http://localhost:8080/auth/google/callback}")
    private String redirectUri;

    @Override
    @Transactional
    public ApiResponse<AuthResponse> googleAuth(GoogleAuthRequest request) {
        log.info("Attempting Google auth with ID token");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(TRANSPORT, JSON_FACTORY)
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(request.getIdToken());
        } catch (Exception e) {
            log.error("Google Token Verification Failed: {}", e.getMessage());
            throw new BadRequestException("Invalid Google token");
        }

        if (idToken == null) {
            throw new BadRequestException("Invalid Google token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail().toLowerCase();
        String googleId = payload.getSubject();

        log.info("Google token verified for email: {}", email);

        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> {
                    if (existingUser.getGoogleId() == null) {
                        existingUser.setGoogleId(googleId);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setGoogleId(googleId);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setEnabled(true);

                    User savedUser = userRepository.save(newUser);
                    walletService.createWalletForUser(savedUser);

                    log.info("Created new user via Google: {}", email);
                    return savedUser;
                });

        return generateAuthResponse(user, "Google authentication successful");
    }

    @Override
    public void initiateGoogleOAuth(HttpServletResponse response) throws IOException {
        log.info("Initiating Google OAuth flow");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                TRANSPORT,
                JSON_FACTORY,
                googleClientId,
                googleClientSecret,
                Collections.singleton("email profile openid")
        ).build();

        String authorizationUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();

        response.sendRedirect(authorizationUrl);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponse> googleCallback(String code) {
        log.info("Processing Google OAuth callback");

        if (code == null || code.isEmpty()) {
            throw new BadRequestException("Authorization code is required");
        }

        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    TRANSPORT,
                    JSON_FACTORY,
                    googleClientId,
                    googleClientSecret,
                    code,
                    redirectUri
            ).execute();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail().toLowerCase();
            String googleId = payload.getSubject();

            log.info("Google OAuth callback verified for email: {}", email);

            User user = userRepository.findByGoogleId(googleId)
                    .or(() -> userRepository.findByEmail(email))
                    .map(existingUser -> {
                        if (existingUser.getGoogleId() == null) {
                            existingUser.setGoogleId(googleId);
                            return userRepository.save(existingUser);
                        }
                        return existingUser;
                    })
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setGoogleId(googleId);
                        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                        newUser.setEnabled(true);

                        User savedUser = userRepository.save(newUser);
                        walletService.createWalletForUser(savedUser);

                        log.info("Created new user via Google OAuth: {}", email);
                        return savedUser;
                    });

            return generateAuthResponse(user, "Google OAuth authentication successful");

        } catch (IOException e) {
            log.error("Failed to process Google OAuth callback: {}", e.getMessage());
            throw new BadRequestException("Failed to process Google OAuth callback");
        }
    }

    private ApiResponse<AuthResponse> generateAuthResponse(User user, String message) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getAuthorities()
        );

        String accessToken = tokenService.generateAccessToken(authentication, user);

        AuthResponse authResponse = AuthResponse.builder()
                .token(accessToken)
                .build();

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .statusCode(200)
                .message(message)
                .data(authResponse)
                .build();
    }
}