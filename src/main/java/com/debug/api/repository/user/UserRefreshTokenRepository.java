package com.debug.api.repository.user;

import com.debug.api.entity.user.User;
import com.debug.api.entity.user.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    Optional<UserRefreshToken> findByUser(User user);
    Optional<UserRefreshToken> findByRefreshTokenAndUser(String refreshToken, User user);
}
