package com.ferrisys.config.license;

import java.util.UUID;

public interface ModuleLicenseService {

    void assertLicensed(UUID tenantId, String moduleCode);
}
