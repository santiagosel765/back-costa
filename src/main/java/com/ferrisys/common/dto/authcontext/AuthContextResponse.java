package com.ferrisys.common.dto.authcontext;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
public class AuthContextResponse {

    private AuthContextUserDto user;
    private AuthContextTenantDto tenant;
    private List<String> roles;
    private List<AuthContextModuleDto> modules;
    private Map<String, List<String>> permissions;
    private AuthContextTokenDto token;
    private Instant serverTime;
}
