package com.ferrisys.common.enums;

import java.util.UUID;

public enum DefaultUserStatus {
    ACTIVE(UUID.fromString("6b393ccc-1eba-4075-9fb2-80091d80f87e")),
    INACTIVE(UUID.fromString("1a83879d-b620-4226-bfa9-146a775183d2"));

    private final UUID id;
    DefaultUserStatus(UUID id) { this.id = id; }
    public UUID getId() { return id; }
}

