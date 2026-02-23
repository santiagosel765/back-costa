package com.ferrisys.common.dto.authcontext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthContextTenantDto {

    private String tenantId;
    private String name;
    private Integer status;
}
