package com.debug.admin.dto.response;

import com.debug.domain.entity.user.User;
import lombok.Builder;

import java.time.LocalDateTime;

public class AdminUserResponse {
    private final Long id;
    private final String userId;
    private final String OAuth2UserId;
    private final String username;
    private final String email;
    private final String emailVerifiedYn;
    private final String profileImageUrl;
    private final String providerType;
    private final String roleType;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;

    public AdminUserResponse(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.OAuth2UserId = user.getOAuth2UserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.emailVerifiedYn = user.getEmailVerifiedYn();
        this.profileImageUrl = user.getProfileImageUrl();
        this.providerType = user.getProviderType().name();
        this.roleType = user.getRoleType().getAuthority();
        this.createdDate = user.getCreatedDate();
        this.modifiedDate = user.getModifiedDate();
    }

    @Builder
    public AdminUserResponse(Long id, String userId, String OAuth2UserId, String username, String email, String emailVerifiedYn, String profileImageUrl, String providerType, String roleType, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.userId = userId;
        this.OAuth2UserId = OAuth2UserId;
        this.username = username;
        this.email = email;
        this.emailVerifiedYn = emailVerifiedYn;
        this.profileImageUrl = profileImageUrl;
        this.providerType = providerType;
        this.roleType = roleType;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
}
