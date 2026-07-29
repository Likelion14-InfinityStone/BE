package com.medipass.server.global.oauth;

import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.jwt.JwtTokenProvider;
import com.medipass.server.global.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * 소셜 로그인 성공 시 JWT(access)를 발급해 프론트로 리다이렉트한다.
 * 로그인 시작 시 저장해둔 출발지(local/deploy) 쿠키를 보고 리다이렉트할 프론트 주소를 고른다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.front-redirect-uri.local}")
    private String localRedirectUri;

    @Value("${app.oauth2.front-redirect-uri.deploy}")
    private String deployRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(
                userDetails.getUser().getId().toString()
        );

        // 출발지(local/deploy) 쿠키 보고 리다이렉트 URI 선택, 누락 시 deploy 폴백
        String target = CookieUtils.getCookie(request,
                        CookieOAuth2AuthorizationRequestRepository.REDIRECT_TARGET_COOKIE)
                .map(Cookie::getValue)
                .orElse(CookieOAuth2AuthorizationRequestRepository.TARGET_DEPLOY);
        String redirectUri = CookieOAuth2AuthorizationRequestRepository.TARGET_LOCAL.equals(target)
                ? localRedirectUri : deployRedirectUri;

        CookieUtils.deleteCookie(request, response,
                CookieOAuth2AuthorizationRequestRepository.REDIRECT_TARGET_COOKIE);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        clearAuthenticationAttributes(request); // 인증 과정에서 세션에 임시 저장된 것 정리
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
