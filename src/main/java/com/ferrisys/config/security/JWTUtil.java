package com.ferrisys.config.security;

import com.ferrisys.common.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    public static final String TENANT_ID_CLAIM = "tenant_id";
    public static final String ROLES_CLAIM = "roles";
    public static final String MODULES_CLAIM = "modules";
    private static final long TOKEN_TTL_MILLIS = 1000L * 60 * 60 * 10;

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User userDetails) {
        return generateToken(userDetails, List.of(), List.of());
    }

    public String generateToken(User userDetails, List<String> roles, List<String> modules) {
        Map<String, Object> claims = new HashMap<>();
        Optional.ofNullable(userDetails.getTenant())
                .map(tenant -> tenant.getId().toString())
                .ifPresent(tenantId -> claims.put(TENANT_ID_CLAIM, tenantId));

        if (roles != null && !roles.isEmpty()) {
            claims.put(ROLES_CLAIM, roles);
        }
        if (modules != null && !modules.isEmpty()) {
            claims.put(MODULES_CLAIM, modules);
        }

        Date expiration = Date.from(Instant.now().plusMillis(TOKEN_TTL_MILLIS));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, String expectedUsername) {
        return expectedUsername.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractTenantId(String token) {
        Claims claims = getClaims(token);
        Object tenantId = claims.get(TENANT_ID_CLAIM);
        if (tenantId == null) {
            tenantId = claims.get("tenantId");
        }
        return tenantId != null ? tenantId.toString() : null;
    }

    public Instant extractExpiration(String token) {
        return getClaims(token).getExpiration().toInstant();
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
