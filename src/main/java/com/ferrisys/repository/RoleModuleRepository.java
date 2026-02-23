package com.ferrisys.repository;

import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthRoleModule;
import com.ferrisys.common.entity.user.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleModuleRepository extends JpaRepository<AuthRoleModule, UUID> {
    void deleteByRole(Role role);

    @Query("SELECT rm.module FROM AuthRoleModule rm WHERE rm.role.id = :roleId AND rm.status = 1")
    Page<AuthModule> findModulesByRoleId(UUID roleId, Pageable pageable);

    @Query("SELECT rm.module.id FROM AuthRoleModule rm WHERE rm.role.id = :roleId AND rm.status = 1")
    List<UUID> findActiveModuleIdsByRoleId(UUID roleId);

    @Query("SELECT DISTINCT rm.module FROM AuthRoleModule rm WHERE rm.role.id IN :roleIds AND rm.status = 1")
    List<AuthModule> findDistinctModulesByRoleIds(Collection<UUID> roleIds);

    List<AuthRoleModule> findByRoleIdAndStatus(UUID roleId, Integer status);
}