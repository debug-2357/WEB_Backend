package com.debug.admin.service.user;

import com.debug.admin.repository.user.AdminUserRepository;
import com.debug.common.exception.UserNotFoundException;
import com.debug.domain.entity.user.User;
import com.debug.oauth.entity.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return adminUserRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return adminUserRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User updateRoleById(Long userId, String roleString) {
        User user = findById(userId);
        RoleType roleType = RoleType.of(roleString);
        user.updateRoleType(roleType);

        return user;
    }
}
