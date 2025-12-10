package dev.slethware.walletservice.service.apikey;

import dev.slethware.walletservice.exception.BadRequestException;
import dev.slethware.walletservice.exception.ResourceNotFoundException;
import dev.slethware.walletservice.exception.UnauthorizedException;
import dev.slethware.walletservice.models.dtos.request.CreateApiKeyRequest;
import dev.slethware.walletservice.models.dtos.request.RolloverApiKeyRequest;
import dev.slethware.walletservice.models.dtos.response.ApiKeyListResponse;
import dev.slethware.walletservice.models.dtos.response.ApiKeyResponse;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.entity.ApiKey;
import dev.slethware.walletservice.models.entity.User;
import dev.slethware.walletservice.repository.ApiKeyRepository;
import dev.slethware.walletservice.repository.UserRepository;
import dev.slethware.walletservice.utility.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final List<String> VALID_PERMISSIONS = Arrays.asList("deposit", "transfer", "read");
    private static final int MAX_ACTIVE_KEYS = 5;

    @Override
    @Transactional
    public ApiResponse<ApiKeyResponse> createApiKey(CreateApiKeyRequest request) {
        User currentUser = getCurrentUser();

        request.getPermissions().forEach(permission -> {
            if (!VALID_PERMISSIONS.contains(permission)) {
                throw new BadRequestException("Invalid permission: " + permission);
            }
        });

        long activeKeyCount = apiKeyRepository.countByUserIdAndRevokedFalseAndExpiresAtAfter(
                currentUser.getId(), LocalDateTime.now());

        if (activeKeyCount >= MAX_ACTIVE_KEYS) {
            throw new BadRequestException("Maximum of 5 active API keys allowed per user");
        }

        String rawApiKey = ApiKeyGenerator.generate();
        String keyHash = passwordEncoder.encode(rawApiKey);

        LocalDateTime expiresAt = calculateExpiry(request.getExpiry());

        ApiKey apiKey = ApiKey.builder()
                .user(currentUser)
                .name(request.getName())
                .keyHash(keyHash)
                .permissions(request.getPermissions())
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        apiKeyRepository.save(apiKey);

        log.info("Created API key for user: {}, expires at: {}", currentUser.getEmail(), expiresAt);

        ApiKeyResponse apiKeyResponse = ApiKeyResponse.builder()
                .apiKey(rawApiKey)
                .expiresAt(expiresAt)
                .build();

        return ApiResponse.<ApiKeyResponse>builder()
                .status("success")
                .statusCode(200)
                .message("API key created successfully")
                .data(apiKeyResponse)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<ApiKeyResponse> rolloverApiKey(RolloverApiKeyRequest request) {
        User currentUser = getCurrentUser();

        UUID expiredKeyId;
        try {
            expiredKeyId = UUID.fromString(request.getExpiredKeyId());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid key ID format");
        }

        ApiKey expiredKey = apiKeyRepository.findById(expiredKeyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        if (!expiredKey.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to rollover this key");
        }

        if (!expiredKey.isExpired()) {
            throw new BadRequestException("Key must be expired to rollover");
        }

        long activeKeyCount = apiKeyRepository.countByUserIdAndRevokedFalseAndExpiresAtAfter(
                currentUser.getId(), LocalDateTime.now());

        if (activeKeyCount >= MAX_ACTIVE_KEYS) {
            throw new BadRequestException("Maximum of 5 active API keys allowed per user");
        }

        String rawApiKey = ApiKeyGenerator.generate();
        String keyHash = passwordEncoder.encode(rawApiKey);

        LocalDateTime expiresAt = calculateExpiry(request.getExpiry());

        ApiKey newApiKey = ApiKey.builder()
                .user(currentUser)
                .name(expiredKey.getName())
                .keyHash(keyHash)
                .permissions(expiredKey.getPermissions())
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        apiKeyRepository.save(newApiKey);

        log.info("Rolled over API key for user: {}, new key expires at: {}", currentUser.getEmail(), expiresAt);

        ApiKeyResponse apiKeyResponse = ApiKeyResponse.builder()
                .apiKey(rawApiKey)
                .expiresAt(expiresAt)
                .build();

        return ApiResponse.<ApiKeyResponse>builder()
                .status("success")
                .statusCode(200)
                .message("API key rolled over successfully")
                .data(apiKeyResponse)
                .build();
    }

    @Override
    public ApiResponse<List<ApiKeyListResponse>> listApiKeys() {
        User currentUser = getCurrentUser();

        List<ApiKey> apiKeys = apiKeyRepository.findByUserIdAndRevokedFalse(currentUser.getId());

        List<ApiKeyListResponse> responses = apiKeys.stream()
                .map(key -> ApiKeyListResponse.builder()
                        .id(key.getId())
                        .name(key.getName())
                        .permissions(key.getPermissions())
                        .expiresAt(key.getExpiresAt())
                        .revoked(key.isRevoked())
                        .createdAt(key.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.<List<ApiKeyListResponse>>builder()
                .status("success")
                .statusCode(200)
                .message("API keys retrieved successfully")
                .data(responses)
                .build();
    }

    @Override
    public ApiResponse<ApiKeyListResponse> getApiKey(UUID keyId) {
        User currentUser = getCurrentUser();

        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        if (!apiKey.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to view this key");
        }

        ApiKeyListResponse response = ApiKeyListResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .permissions(apiKey.getPermissions())
                .expiresAt(apiKey.getExpiresAt())
                .revoked(apiKey.isRevoked())
                .createdAt(apiKey.getCreatedAt())
                .build();

        return ApiResponse.<ApiKeyListResponse>builder()
                .status("success")
                .statusCode(200)
                .message("API key retrieved successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> revokeApiKey(UUID keyId) {
        User currentUser = getCurrentUser();

        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        if (!apiKey.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to revoke this key");
        }

        if (apiKey.isRevoked()) {
            throw new BadRequestException("API key is already revoked");
        }

        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);

        log.info("Revoked API key {} for user: {}", keyId, currentUser.getEmail());

        return ApiResponse.<Void>builder()
                .status("success")
                .statusCode(200)
                .message("API key revoked successfully")
                .build();
    }

    @Override
    public ApiKey validateApiKey(String apiKeyString) {
        if (apiKeyString == null || !apiKeyString.startsWith("sk_live_")) {
            return null;
        }

        List<ApiKey> activeKeys = apiKeyRepository.findByUserIdAndRevokedFalse(null).stream()
                .filter(key -> !key.isExpired() && !key.isRevoked())
                .toList();

        for (ApiKey apiKey : activeKeys) {
            if (passwordEncoder.matches(apiKeyString, apiKey.getKeyHash())) {
                if (apiKey.isActive()) {
                    return apiKey;
                }
            }
        }

        List<ApiKey> allActiveKeys = apiKeyRepository.findAll().stream()
                .filter(key -> !key.isExpired() && !key.isRevoked())
                .toList();

        for (ApiKey apiKey : allActiveKeys) {
            if (passwordEncoder.matches(apiKeyString, apiKey.getKeyHash())) {
                if (apiKey.isActive()) {
                    return apiKey;
                }
            }
        }

        return null;
    }

    private LocalDateTime calculateExpiry(String expiry) {
        LocalDateTime now = LocalDateTime.now();
        return switch (expiry) {
            case "1H" -> now.plusHours(1);
            case "1D" -> now.plusDays(1);
            case "1M" -> now.plusMonths(1);
            case "1Y" -> now.plusYears(1);
            default -> throw new BadRequestException("Invalid expiry format");
        };
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}