package com.ferrisys.repository;

import com.ferrisys.common.entity.user.AuthUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRoleRepository extends JpaRepository<AuthUserRole, UUID> {

    Optional<AuthUserRole> findByUserId(UUID userId);

    List<AuthUserRole> findAllByUserIdIn(Collection<UUID> userIds);

    void deleteByUserId(UUID userId);

    @Query("SELECT ur.role.id FROM AuthUserRole ur WHERE ur.user.id = :userId AND ur.status = 1")
    List<UUID> findActiveRoleIdsByUserId(UUID userId);

    @Query("SELECT ur.role.name FROM AuthUserRole ur WHERE ur.user.id = :userId AND ur.status = 1")
    List<String> findActiveRoleNamesByUserId(UUID userId);
}
