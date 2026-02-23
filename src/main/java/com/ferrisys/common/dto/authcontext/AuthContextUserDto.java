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
    private UUID status;
}
