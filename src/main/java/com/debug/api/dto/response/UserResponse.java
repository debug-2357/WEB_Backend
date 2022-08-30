package com.debug.api.dto.response;

import com.debug.domain.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String userId;
    private final String username;
    private final String email;
    private final String profileImageUrl;
    private final String role;

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRoleType().getAuthority();
    }

    @Builder
    public UserResponse(String userId, String username, String email, String profileImageUrl, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
    }
}
