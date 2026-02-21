package com.ferrisys.repository;

import com.ferrisys.common.entity.user.AuthModule;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<AuthModule, UUID> {

    Optional<AuthModule> findByNameIgnoreCase(String name);
}
