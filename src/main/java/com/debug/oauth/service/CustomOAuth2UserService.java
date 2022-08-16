package com.debug.oauth.service;

import com.debug.api.entity.user.User;
import com.debug.api.repository.user.UserRepository;
import com.debug.oauth.entity.ProviderType;
import com.debug.oauth.entity.RoleType;
import com.debug.oauth.entity.UserPrincipal;
import com.debug.oauth.exception.OAuth2ProviderMissMatchException;
import com.debug.oauth.info.OAuth2UserInfo;
import com.debug.oauth.info.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        try {
            return this.process(userRequest, user);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    /**
     * userRequest와 user를 바탕으로 유저를 가공(생성 혹은 수정)해서 반환
     * @param userRequest request
     * @param user user
     * @return OAuth2User(UserPrincipal)
     */
    public OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        User savedUser = userRepository.findByOAuth2UserId(userInfo.getId()).orElse(null);

        // 저장된 유저가 있으면
        if (savedUser != null) {
            // 저장된 유저의 provider과 OAuth2 서버에 요청한 provider가 다르면 오류 발생
            if (providerType != savedUser.getProviderType()) {
                throw new OAuth2ProviderMissMatchException(
                        "Looks like you're signed up with " + providerType +
                                " account. Please use your " + savedUser.getProviderType() + " account to login."
                );
            }

            // request의 내용을 최신화 한다.(값이 null이거나 바꿀필요 없으면 그대로 냅둠)
            savedUser.updateNameAndImageUrl(userInfo.getName(), userInfo.getImageUrl());

        // 저장된 유저가 없으면
        } else {
            // 정보를 토대로 저장한다
            savedUser = createUser(userInfo, providerType);
        }

        return UserPrincipal.create(savedUser, user.getAttributes());
    }

    private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
        User user = User.builder()
                .userId(userInfo.getId())
                .OAuth2UserId(userInfo.getId())
                .username(userInfo.getName())
                .password(new BCryptPasswordEncoder().encode("NO_PASS"))
                .email(Optional.ofNullable(userInfo.getEmail()).orElse("NO_EMAIL"))
                .emailVerifiedYn("Y")
                .profileImageUrl(Optional.ofNullable(userInfo.getImageUrl()).orElse(""))
                .providerType(providerType)
                .roleType(RoleType.GUEST)
                .build();

        return userRepository.saveAndFlush(user);
    }
}
