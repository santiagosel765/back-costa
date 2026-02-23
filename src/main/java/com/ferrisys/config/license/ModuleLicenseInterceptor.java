package com.ferrisys.config.license;

import com.ferrisys.core.tenant.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ModuleLicenseInterceptor implements HandlerInterceptor {

    private final ModuleResolver moduleResolver;
    private final ModuleLicenseService moduleLicenseService;
    private final TenantContextHolder tenantContextHolder;

    public ModuleLicenseInterceptor(
            ModuleResolver moduleResolver,
            ModuleLicenseService moduleLicenseService,
            TenantContextHolder tenantContextHolder) {
        this.moduleResolver = moduleResolver;
        this.moduleLicenseService = moduleLicenseService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional<String> moduleCode = moduleResolver.resolve(request);
        if (moduleCode.isEmpty()) {
            return true;
        }

        UUID tenantId = tenantContextHolder.requireTenantId();
        moduleLicenseService.assertLicensed(tenantId, moduleCode.get());
        return true;
    }
}
