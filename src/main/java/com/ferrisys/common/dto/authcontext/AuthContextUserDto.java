package com.ferrisys.common.dto.authcontext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthContextUserDto {

    private String id;
    private String username;
    private String fullName;
    private String email;
    /**
     * Legacy compatibility field consumed by frontend. It now returns a stable status key (e.g. ACTIVE).
     */
    private String status;
    private UUID statusId;
    private String statusKey;
    private String statusLabel;
}
