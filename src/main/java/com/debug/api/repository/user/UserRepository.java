package com.debug.api.repository.user;

import com.debug.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByOAuth2UserId(String OAuth2UserId);
    boolean existsByUserId(String userId);
    boolean existsByOAuth2UserId(String OAuth2UserId);
}
