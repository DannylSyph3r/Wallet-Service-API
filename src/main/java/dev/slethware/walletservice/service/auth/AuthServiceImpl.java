package dev.slethware.walletservice.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public ApiResponse<AuthResponse> googleAuth(GoogleAuthRequest request) {
        log.info("Attempting Google auth");

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
                        existingUser.setEnabled(true);
                        log.info("Linking existing user to Google ID: {}", email);
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

                    log.info("Created new Google user: {}", email);
                    User savedUser = userRepository.save(newUser);

                    walletService.createWalletForUser(savedUser);

                    return savedUser;
                });

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
                .message("Authentication successful")
                .data(authResponse)
                .build();
    }
}