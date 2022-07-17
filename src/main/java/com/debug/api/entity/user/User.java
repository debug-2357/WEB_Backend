package com.debug.api.entity.user;

import com.debug.api.entity.BaseTimeEntity;
import com.debug.oauth.entity.ProviderType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_id", length = 64, unique = true, nullable = false)
    private String userId;

    @Column(name = "username", length = 100, unique = true, nullable = false)
    private String username;

    @Column(name = "password", length = 128, nullable = false)
    private String password;

    @Column(name = "email", length = 512, unique = true, nullable = false)
    private String email;

    @Column(name = "email_verified_yn", length = 1, nullable = false)
    private String emailVerifiedYn;

    @Column(name = "profile_image_url", length = 512, nullable = false)
    private String profileImageUrl;

    @Column(name = "provider_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderType roleType;

    @Builder
    public User(Long id, String userId, String username, String password, String email, String emailVerifiedYn, String profileImageUrl, ProviderType roleType) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.emailVerifiedYn = emailVerifiedYn;
        this.profileImageUrl = profileImageUrl;
        this.roleType = roleType;
    }
}
