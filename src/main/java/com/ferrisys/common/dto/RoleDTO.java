package com.ferrisys.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class RoleDTO {

    UUID id;

    @NotBlank(message = "name is required")
    String name;

    @NotBlank(message = "description is required")
    String description;

    @NotNull(message = "status is required")
    Integer status;

    List<UUID> moduleIds;
}
