package com.debug.api.service;

import com.debug.api.dto.request.RegisterRequest;
import com.debug.api.dto.response.UserResponse;
import com.debug.api.entity.user.User;
import com.debug.api.exception.UserNotFoundException;
import com.debug.api.repository.user.UserRepository;
import com.debug.oauth.entity.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getByUserId(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(
                UserNotFoundException::new
        );

        return new UserResponse(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Transactional
    public void createUser(RegisterRequest registerRequest) {
        User user = User.builder()
                .userId(registerRequest.getUserId())
                .OAuth2UserId("")
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword1()))
                .email(registerRequest.getEmail())
                .emailVerifiedYn("Y")
                .profileImageUrl("")
                .providerType(null)
                .roleType(RoleType.GUEST)
                .build();

        userRepository.save(user);
    }
}
