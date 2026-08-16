package com.medipass.server.domain.user.web.dto;

import com.medipass.server.domain.user.entity.User;

public record UserAccountRes(
        String nickName,
        String email,
        String profileImageUrl
) {

    public static UserAccountRes from(User user) {
        return new UserAccountRes(
                user.getNickName(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }
}
