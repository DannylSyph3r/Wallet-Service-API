package dev.slethware.walletservice.security;

import dev.slethware.walletservice.models.entity.ApiKey;
import dev.slethware.walletservice.service.apikey.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String apiKeyHeader = request.getHeader("x-api-key");

            if (apiKeyHeader != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                ApiKey apiKey = apiKeyService.validateApiKey(apiKeyHeader);

                if (apiKey != null && apiKey.isActive()) {
                    ApiKeyAuthentication authentication = new ApiKeyAuthentication(
                            apiKey.getUser(),
                            apiKey.getPermissions()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set API key authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}