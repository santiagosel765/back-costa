package com.ferrisys.config.security;

import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthUserRole;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.repository.RoleModuleRepository;
import com.ferrisys.service.FeatureFlagService;
import com.ferrisys.service.UserService;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final RoleModuleRepository roleModuleRepository;
    private final FeatureFlagService featureFlagService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userService.getAuthUser(username);
            AuthUserRole userRole = userService.getUserRole(user.getId());
            Role role = userRole.getRole();

            Set<GrantedAuthority> authorities = new HashSet<>();
            if (role != null && role.getName() != null && !role.getName().isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + normalize(role.getName())));
            }

            if (role != null && role.getId() != null) {
                roleModuleRepository.findModulesByRoleId(role.getId(), Pageable.unpaged())
                        .forEach(module -> addModuleAuthority(authorities, module, user.getTenant() != null ? user.getTenant().getId() : null));
            }

            if (log.isDebugEnabled()) {
                log.debug("Authorities for {}: {}", username, authorities);
            }

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountLocked(false)
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (NotFoundException ex) {
            throw new UsernameNotFoundException(ex.getMessage(), ex);
        }
    }

    private void addModuleAuthority(Set<GrantedAuthority> authorities, AuthModule module, UUID tenantId) {
        if (module != null && module.getName() != null && !module.getName().isBlank()
                && featureFlagService.isModuleEnabled(tenantId, module.getName())) {
            authorities.add(new SimpleGrantedAuthority("MODULE_" + normalize(module.getName())));
        }
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.toUpperCase(Locale.ROOT);
    }
}
