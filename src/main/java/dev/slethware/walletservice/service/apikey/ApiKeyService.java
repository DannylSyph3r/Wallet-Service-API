package dev.slethware.walletservice.service.apikey;

import dev.slethware.walletservice.models.dtos.request.CreateApiKeyRequest;
import dev.slethware.walletservice.models.dtos.request.RolloverApiKeyRequest;
import dev.slethware.walletservice.models.dtos.response.ApiKeyResponse;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.models.entity.ApiKey;

public interface ApiKeyService {
    ApiResponse<ApiKeyResponse> createApiKey(CreateApiKeyRequest request);
    ApiResponse<ApiKeyResponse> rolloverApiKey(RolloverApiKeyRequest request);
    ApiKey validateApiKey(String apiKeyString);
}