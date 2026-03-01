package com.ferrisys.service.impl;

import com.ferrisys.common.entity.license.ModuleLicense;
import com.ferrisys.common.util.ModuleKeyNormalizer;
import com.ferrisys.core.tenant.TenantContext;
import com.ferrisys.repository.ModuleLicenseRepository;
import com.ferrisys.service.FeatureFlagService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service("featureFlagService")
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private static final long CACHE_TTL_SECONDS = 60;

    private final ModuleLicenseRepository moduleLicenseRepository;
    private final ConcurrentMap<UUID, CachedModules> tenantModuleCache = new ConcurrentHashMap<>();

    public FeatureFlagServiceImpl(ModuleLicenseRepository moduleLicenseRepository) {
        this.moduleLicenseRepository = moduleLicenseRepository;
    }

    @Override
    public boolean isModuleEnabled(String moduleKey) {
        return isModuleEnabled(getCurrentTenantIdOrThrow(), moduleKey);
    }

    @Override
    public boolean isModuleEnabled(UUID tenantId, String moduleKey) {
        if (tenantId == null || moduleKey == null || moduleKey.isBlank()) {
            return false;
        }

        String normalizedKey = ModuleKeyNormalizer.normalize(moduleKey);
        CachedModules cachedModules = tenantModuleCache.compute(tenantId, (id, cached) -> {
            if (cached == null || cached.isExpired()) {
                return loadTenantModules(id);
            }
            return cached;
        });

        return cachedModules.modules().getOrDefault(normalizedKey, Boolean.TRUE);
    }

    @Override
    public void assertModuleEnabled(String moduleKey) {
        if (!isModuleEnabled(moduleKey)) {
            throw new AccessDeniedException("Module '%s' is disabled for the current tenant".formatted(moduleKey));
        }
    }

    @Override
    public void evictTenantCache(UUID tenantId) {
        if (tenantId != null) {
            tenantModuleCache.remove(tenantId);
        }
    }

    private CachedModules loadTenantModules(UUID tenantId) {
        Map<String, Boolean> enabledMap = new HashMap<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (ModuleLicense license : moduleLicenseRepository.findAllByTenantIdWithModule(tenantId)) {
            if (license.getModule() == null || license.getModule().getModuleKey() == null) {
                continue;
            }

            boolean enabled = Boolean.TRUE.equals(license.getEnabled());
            OffsetDateTime startAt = license.getStartAt();
            OffsetDateTime endAt = license.getEndAt() != null ? license.getEndAt() : license.getExpiresAt();

            if (startAt != null && startAt.isAfter(now)) {
                enabled = false;
            }
            if (endAt != null && endAt.isBefore(now)) {
                enabled = false;
            }

            enabledMap.put(ModuleKeyNormalizer.normalize(license.getModule().getModuleKey()), enabled);
        }

        return new CachedModules(enabledMap, Instant.now().plusSeconds(CACHE_TTL_SECONDS));
    }

    private UUID getCurrentTenantIdOrThrow() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new AccessDeniedException("Tenant context is required");
        }
        return UUID.fromString(tenantId);
    }

    private record CachedModules(Map<String, Boolean> modules, Instant expiresAt) {

        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
