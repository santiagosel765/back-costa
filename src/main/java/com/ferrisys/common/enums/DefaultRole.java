package com.ferrisys.common.enums;

import java.util.UUID;

public enum DefaultRole {
    USER(UUID.fromString("08b21056-7149-40bd-86f4-7be597071d55")),
    ADMIN(UUID.fromString("20bda0bd-c44b-4e46-af5f-d77697a2f7b2"));

    private final UUID id;

    DefaultRole(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
