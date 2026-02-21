package com.ferrisys.config.security;

import com.ferrisys.common.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    public static final String TENANT_ID_CLAIM = "tenant_id";

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Optional.ofNullable(userDetails.getTenant())
                .map(tenant -> tenant.getId().toString())
                .ifPresent(tenantId -> claims.put(TENANT_ID_CLAIM, tenantId));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
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
