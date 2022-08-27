package com.debug.api.service.user;

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

import java.util.UUID;

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
        boolean isExistUser = true;
        String randomString = null;

        while (isExistUser) {
            randomString = UUID.randomUUID().toString();
            if (!userRepository.existsByOAuth2UserId(randomString)) {
                isExistUser = false;
            }
        }

        User user = User.builder()
                .userId(registerRequest.getUserId())
                .OAuth2UserId(randomString)
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword1()))
                .email(registerRequest.getEmail())
                .emailVerifiedYn("Y")
                .profileImageUrl("")
                .providerType(null)
                .roleType(RoleType.UNCONFIRMED)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public void changeUnconfirmed(String userId, RegisterRequest registerRequest) {
        User user = userRepository.findByUserId(userId).orElseThrow(
                UserNotFoundException::new
        );

        user.changeUnconfirmed(
                registerRequest.getUserId(),
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword1()),
                registerRequest.getEmail()
        );
    }
}
