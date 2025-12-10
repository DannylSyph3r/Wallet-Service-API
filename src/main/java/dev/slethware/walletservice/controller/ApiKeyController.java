package dev.slethware.walletservice.controller;

import dev.slethware.walletservice.models.dtos.request.CreateApiKeyRequest;
import dev.slethware.walletservice.models.dtos.request.RolloverApiKeyRequest;
import dev.slethware.walletservice.models.dtos.response.ApiKeyListResponse;
import dev.slethware.walletservice.models.dtos.response.ApiKeyResponse;
import dev.slethware.walletservice.models.dtos.response.ApiResponse;
import dev.slethware.walletservice.service.apikey.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/keys")
@RequiredArgsConstructor
@Tag(name = "API Key Management", description = "Endpoints for managing API keys")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/create")
    @Operation(
            summary = "Create API Key",
            description = "Create a new API key with specific permissions. Maximum 5 active keys per user."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API key created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or key limit reached"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.ok(apiKeyService.createApiKey(request));
    }

    @PostMapping("/rollover")
    @Operation(
            summary = "Rollover Expired API Key",
            description = "Create a new API key using the same permissions as an expired key."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API key rolled over successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Key not expired or invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiResponse<ApiKeyResponse>> rolloverApiKey(@Valid @RequestBody RolloverApiKeyRequest request) {
        return ResponseEntity.ok(apiKeyService.rolloverApiKey(request));
    }

    @GetMapping
    @Operation(
            summary = "List API Keys",
            description = "List all API keys for the authenticated user. Does not show actual API key values."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API keys retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<ApiKeyListResponse>>> listApiKeys() {
        return ResponseEntity.ok(apiKeyService.listApiKeys());
    }

    @GetMapping("/{keyId}")
    @Operation(
            summary = "Get API Key Details",
            description = "Get details of a specific API key. Does not show actual API key value."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API key retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to view this key"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiResponse<ApiKeyListResponse>> getApiKey(
            @Parameter(description = "API Key ID") @PathVariable UUID keyId) {
        return ResponseEntity.ok(apiKeyService.getApiKey(keyId));
    }

    @PostMapping("/{keyId}/revoke")
    @Operation(
            summary = "Revoke API Key",
            description = "Revoke an API key, making it immediately unusable regardless of expiry time."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API key revoked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API key already revoked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to revoke this key"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiResponse<Void>> revokeApiKey(
            @Parameter(description = "API Key ID") @PathVariable UUID keyId) {
        return ResponseEntity.ok(apiKeyService.revokeApiKey(keyId));
    }
}