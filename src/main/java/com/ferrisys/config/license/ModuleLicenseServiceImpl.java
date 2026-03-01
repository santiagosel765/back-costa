package com.ferrisys.config.license;

import com.ferrisys.common.entity.license.ModuleLicense;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.exception.ModuleNotLicensedException;
import com.ferrisys.repository.ModuleLicenseRepository;
import com.ferrisys.repository.ModuleRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ModuleLicenseServiceImpl implements ModuleLicenseService {

    private final ModuleRepository moduleRepository;
    private final ModuleLicenseRepository moduleLicenseRepository;

    public ModuleLicenseServiceImpl(ModuleRepository moduleRepository, ModuleLicenseRepository moduleLicenseRepository) {
        this.moduleRepository = moduleRepository;
        this.moduleLicenseRepository = moduleLicenseRepository;
    }

    @Override
    public void assertLicensed(UUID tenantId, String moduleCode) {
        AuthModule module = moduleRepository.findByModuleKeyIgnoreCaseAndTenantId(moduleCode, tenantId)
                .orElseThrow(() -> new ModuleNotLicensedException("Module not licensed"));

        ModuleLicense moduleLicense = moduleLicenseRepository.findByTenantIdAndModule_Id(tenantId, module.getId())
                .orElseThrow(() -> new ModuleNotLicensedException("Module not licensed"));

        OffsetDateTime now = OffsetDateTime.now();
        if (!Boolean.TRUE.equals(moduleLicense.getEnabled())) {
            throw new ModuleNotLicensedException("Module not licensed");
        }
        if (moduleLicense.getStartAt() != null && moduleLicense.getStartAt().isAfter(now)) {
            throw new ModuleNotLicensedException("Module not licensed");
        }
        OffsetDateTime validUntil = moduleLicense.getEndAt() != null
                ? moduleLicense.getEndAt()
                : moduleLicense.getExpiresAt();
        if (validUntil != null && validUntil.isBefore(now)) {
            throw new ModuleNotLicensedException("Module not licensed");
        }
    }
}
