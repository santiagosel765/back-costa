package com.ferrisys.common.dto.auth;

import java.util.Map;
import java.util.UUID;
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
public class RolePermissionsDto {

    private UUID roleId;
    private String roleName;
    private Map<String, ActionPermissionsDto> permissions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionPermissionsDto {
        private boolean read;
        private boolean write;
        private boolean delete;
    }
}
