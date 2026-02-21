package com.ferrisys.service;

import com.ferrisys.common.dto.AuthResponse;
import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.RegisterRequest;
import com.ferrisys.common.entity.user.AuthUserRole;
import com.ferrisys.common.entity.user.User;
import java.util.UUID;

public interface UserService {

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    void changePassword(UUID userId, String newPassword, String confirmPassword);

    User getAuthUser(String username);

    AuthUserRole getUserRole(UUID userId);

    AuthResponse registerUser(RegisterRequest request);

    AuthResponse authenticate(String username, String password);

    AuthResponse changePasswordForCurrentUser(String currentPassword, String newPassword);

    PageResponse<ModuleDTO> getModulesForCurrentUser(int page, int size);
}
