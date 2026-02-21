package com.ferrisys.service;

import java.util.UUID;

public interface FeatureFlagService {

    boolean enabled(UUID tenantId, String moduleSlug);

    boolean enabledForCurrentUser(String moduleSlug);
}
