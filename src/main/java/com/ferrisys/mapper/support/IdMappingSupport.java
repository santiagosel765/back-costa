package com.ferrisys.mapper.support;

import java.util.UUID;

public interface IdMappingSupport {
    default UUID toUuid(String id) {
        return (id == null || id.isBlank()) ? null : UUID.fromString(id);
    }

    default String fromUuid(UUID id) {
        return id == null ? null : id.toString();
    }
}
