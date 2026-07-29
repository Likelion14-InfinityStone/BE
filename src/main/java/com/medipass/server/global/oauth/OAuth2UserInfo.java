package com.medipass.server.global.oauth;

import com.medipass.server.domain.user.entity.UserProvider;

public interface OAuth2UserInfo {
    UserProvider getProvider();
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImageUrl();
}
