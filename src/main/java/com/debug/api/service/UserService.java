package com.debug.api.service;

import com.debug.api.dto.response.UserResponse;
import com.debug.api.entity.user.User;
import com.debug.api.exception.UserNotFoundException;
import com.debug.api.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getByUserId(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(
                UserNotFoundException::new
        );

        return new UserResponse(user);
    }
}
