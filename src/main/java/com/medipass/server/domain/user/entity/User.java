package com.medipass.server.domain.user.entity;

import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private UserProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(name = "profile_image_url", nullable = true) // 사용자가 설정하지 않으면 null로 넘어옴
    private String profileImageUrl;

    public static User createOAuth(UserProvider provider, String providerId, String email, String nickName) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickName(nickName)
                .build();
    }
}
