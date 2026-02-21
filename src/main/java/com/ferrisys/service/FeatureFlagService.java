package com.ferrisys.service;

import java.util.UUID;

public interface FeatureFlagService {

    boolean isModuleEnabled(String moduleKey);

    boolean isModuleEnabled(UUID tenantId, String moduleKey);

    void assertModuleEnabled(String moduleKey);

    void evictTenantCache(UUID tenantId);

    default boolean enabled(UUID tenantId, String moduleSlug) {
        return isModuleEnabled(tenantId, moduleSlug);
    }

    default boolean enabledForCurrentUser(String moduleSlug) {
        return isModuleEnabled(moduleSlug);
    }
}
