package com.debug.api.service.user;

import com.debug.domain.entity.user.User;
import com.debug.domain.entity.user.UserRefreshToken;
import com.debug.common.exception.RefreshTokenNotFoundException;
import com.debug.common.exception.UserNotFoundException;
import com.debug.api.repository.user.UserRefreshTokenRepository;
import com.debug.api.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRefreshTokenService {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    @Transactional(readOnly = true)
    public UserRefreshToken findByUserId(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(
                UserNotFoundException::new
        );
        return userRefreshTokenRepository.findByUser(user).orElseThrow(
                RefreshTokenNotFoundException::new
        );

    }

    @Transactional(readOnly = true)
    public UserRefreshToken findByRefreshTokenAndUserId(String refreshToken, String userId) {
        User user = userRepository.findByUserId(userId).orElseGet(
                () -> userRepository.findByOAuth2UserId(userId).orElseThrow(
                        UserNotFoundException::new
                )
        );

        return userRefreshTokenRepository.findByRefreshTokenAndUser(refreshToken, user).orElseThrow(
                RefreshTokenNotFoundException::new
        );
    }

    @Transactional
    public void save(String refreshToken, String userId) {
        User user = userRepository.findByUserId(userId).orElseGet(
                () -> userRepository.findByOAuth2UserId(userId).orElseThrow(
                        UserNotFoundException::new
                )
        );
        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUser(user).orElse(null);

        if (userRefreshToken != null) {
            // 리프래쉬 토큰이 db에 존재하면 업데이트
            userRefreshToken.updateRefreshToken(refreshToken);
        } else {
            // db에 존재하지않으면 db에 저장
            userRefreshToken = UserRefreshToken.builder()
                    .user(user)
                    .refreshToken(refreshToken)
                    .build();
        }
        userRefreshTokenRepository.saveAndFlush(userRefreshToken);
    }

}
