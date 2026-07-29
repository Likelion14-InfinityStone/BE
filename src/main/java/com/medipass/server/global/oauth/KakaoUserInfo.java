package com.medipass.server.global.oauth;

import com.medipass.server.domain.user.entity.UserProvider;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public UserProvider getProvider() {
        return UserProvider.KAKAO;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) return null;
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) return null;

        Map<String, Object> profile = getProfile(kakaoAccount);
        if (profile == null) return null;
        return (String) profile.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) return null;

        Map<String, Object> profile = getProfile(kakaoAccount);
        if (profile == null) return null;
        return (String) profile.get("profile_image_url");
    }

    // ---------- 메서드 ----------
    private Map<String, Object> getKakaoAccount() {
        return (Map<String, Object>) attributes.get("kakao_account");
    }

    private Map<String, Object> getProfile(Map<String, Object> kakaoAccount) {
        return (Map<String, Object>) kakaoAccount.get("profile");
    }
}
