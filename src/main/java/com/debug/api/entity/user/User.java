package com.debug.api.entity.user;

import com.debug.api.entity.BaseTimeEntity;
import com.debug.oauth.entity.ProviderType;
import com.debug.oauth.entity.RoleType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_id", length = 64, unique = true, nullable = false)
    private String userId;

    @Column(name = "oauth2_user_id", length = 64, unique = true, nullable = false)
    private String OAuth2UserId;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "password", length = 128, nullable = false)
    private String password;

    @Column(name = "email", length = 512, unique = true, nullable = false)
    private String email;

    @Column(name = "email_verified_yn", length = 1, nullable = false)
    private String emailVerifiedYn;

    @Column(name = "profile_image_url", length = 512, nullable = false)
    private String profileImageUrl;

    @Column(name = "provider_type", length = 20)
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(name = "role_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Builder
    public User(String userId, String OAuth2UserId, String username, String password, String email, String emailVerifiedYn, String profileImageUrl, ProviderType providerType, RoleType roleType) {
        this.userId = userId;
        this.OAuth2UserId = OAuth2UserId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.emailVerifiedYn = emailVerifiedYn;
        this.profileImageUrl = profileImageUrl;
        this.providerType = providerType;
        this.roleType = roleType;
    }

    public void updateNameAndImageUrl(String username, String profileImageUrl) {
        if (username != null && !getUsername().equals(username)) {
            this.username = username;
        }

        if (profileImageUrl != null && !getProfileImageUrl().equals(profileImageUrl)) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void changeUnconfirmed(String userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roleType = RoleType.UNCONFIRMED;
    }
}
