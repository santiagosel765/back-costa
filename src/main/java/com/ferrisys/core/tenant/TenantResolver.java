package com.ferrisys.core.tenant;

import com.ferrisys.config.security.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantResolver {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final JWTUtil jwtUtil;
    private final Environment environment;

    @Value("${app.tenant.allow-header-fallback:false}")
    private boolean allowHeaderFallback;

    public Optional<String> resolve(HttpServletRequest request, String jwt) {
        if (jwt != null && !jwt.isBlank()) {
            Claims claims = jwtUtil.getClaims(jwt);
            Object tenantClaim = claims.get("tenant_id");
            if (tenantClaim == null) {
                tenantClaim = claims.get("tenantId");
            }
            if (tenantClaim != null && !tenantClaim.toString().isBlank()) {
                return Optional.of(tenantClaim.toString());
            }
        }

        if (allowHeaderFallback && isDevProfile()) {
            String header = request.getHeader(TENANT_HEADER);
            if (header != null && !header.isBlank()) {
                return Optional.of(header);
            }
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    private boolean isDevProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("local"));
    }
}
