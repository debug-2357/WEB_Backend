package com.debug.api.dto.response;

import com.debug.api.entity.user.User;
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
}
