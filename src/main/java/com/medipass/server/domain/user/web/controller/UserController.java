package com.medipass.server.domain.user.web.controller;

import com.medipass.server.domain.user.service.UserService;
import com.medipass.server.domain.user.web.dto.UserAccountRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 계정 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 계정 정보 조회",
            description = "더보기 화면에 표시할 로그인 사용자의 닉네임, 이메일, 프로필 이미지 URL을 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<UserAccountRes> getMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserAccountRes response = userService.getAccount(userDetails.getUser().getId());
        return ApiResponse.success(response);
    }
}
