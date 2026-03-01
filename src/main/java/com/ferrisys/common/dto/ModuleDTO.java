package com.ferrisys.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModuleDTO(
        String id,
        @NotBlank(message = "moduleKey is required") String moduleKey,
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "description is required") String description,
        @NotNull(message = "status is required") Integer status) {
}
