package com.ferrisys.config.license;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ModuleResolver {

    private static final Map<String, String> MODULE_BY_PATH_PREFIX = new LinkedHashMap<>();

    static {
        MODULE_BY_PATH_PREFIX.put("/v1/clients", "clients");
        MODULE_BY_PATH_PREFIX.put("/v1/providers", "providers");
        MODULE_BY_PATH_PREFIX.put("/v1/config", "config");
        MODULE_BY_PATH_PREFIX.put("/v1/org", "org");
    }

    public Optional<String> resolve(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || isPublicPath(path)) {
            return Optional.empty();
        }

        return MODULE_BY_PATH_PREFIX.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/v1/auth/login")
                || path.startsWith("/actuator/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
