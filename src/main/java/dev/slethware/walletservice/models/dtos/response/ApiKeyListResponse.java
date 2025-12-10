package dev.slethware.walletservice.models.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyListResponse {
    private UUID id;
    private String name;
    private List<String> permissions;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime createdAt;
}