package com.ferrisys.common.dto.authcontext;

import java.time.OffsetDateTime;
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
public class AuthContextModuleDto {

    private String key;
    private String label;
    private Boolean enabled;
    private OffsetDateTime expiresAt;
    private String baseRoute;
    private Integer sortOrder;
}
