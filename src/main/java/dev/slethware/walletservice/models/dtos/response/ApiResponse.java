package dev.slethware.walletservice.models.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String status,
        int statusCode,
        String message,
        T data
) {}