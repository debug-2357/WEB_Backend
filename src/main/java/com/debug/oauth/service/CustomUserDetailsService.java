package com.debug.oauth.service;

import com.debug.domain.entity.user.User;
import com.debug.common.exception.LoginFailedException;
import com.debug.api.repository.user.UserRepository;
import com.debug.oauth.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUserId(username).orElseThrow(
                LoginFailedException::new
        );
        
        return UserPrincipal.create(user);
    }
}
