package com.ferrisys.core.tenant;

import com.ferrisys.common.exception.impl.BadRequestException;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TenantContextHolder {

    public UUID requireTenantId() {
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) {
            throw new BadRequestException("Tenant context is missing for authenticated request");
        }
        return UUID.fromString(tenant);
    }
}
