package dev.slethware.walletservice.service.apikey;

import dev.slethware.walletservice.models.dtos.request.CreateApiKeyRequest;
import dev.slethware.walletservice.models.dtos.request.RolloverApiKeyRequest;
import dev.slethware.walletservice.models.dtos.response.ApiKeyListResponse;
import dev.slethware.walletservice.models.dtos.response.ApiKeyResponse;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.entity.ApiKey;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiResponse<ApiKeyResponse> createApiKey(CreateApiKeyRequest request);
    ApiResponse<ApiKeyResponse> rolloverApiKey(RolloverApiKeyRequest request);
    ApiResponse<List<ApiKeyListResponse>> listApiKeys();
    ApiResponse<ApiKeyListResponse> getApiKey(UUID keyId);
    ApiResponse<Void> revokeApiKey(UUID keyId);
    ApiKey validateApiKey(String apiKeyString);
}