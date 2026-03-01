package com.ferrisys.repository;

import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.entity.user.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    Optional<User> findByUsernameAndStatus(String username, UserStatus status);

    Optional<User> findByUsernameAndStatusAndTenant_Id(String username, UserStatus status, UUID tenantId);

    List<User> findByTenant_IdAndIdIn(UUID tenantId, Collection<UUID> ids);
}
