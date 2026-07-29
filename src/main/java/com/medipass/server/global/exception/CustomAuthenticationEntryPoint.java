package com.medipass.server.global.exception;

import tools.jackson.databind.ObjectMapper;
import com.medipass.server.global.jwt.JwtAuthenticationFilter;
import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.code.ErrorResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // JSON 직렬화 도구

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        // 필터에서 넣어둔 예외 정보 꺼내기
        String exception = (String) request.getAttribute(JwtAuthenticationFilter.EXCEPTION_ATTRIBUTE);

        // 예외 종류에 따라 에러 코드 결정
        ErrorResponseCode errorCode;
        if (JwtAuthenticationFilter.EXPIRED_TOKEN.equals(exception)) {
            errorCode = ErrorResponseCode.EXPIRED_TOKEN;   // 만료된 토큰
        } else if (JwtAuthenticationFilter.INVALID_TOKEN.equals(exception)) {
            errorCode = ErrorResponseCode.INVALID_TOKEN;   // 위조/형식 오류 토큰
        } else {
            errorCode = ErrorResponseCode.UNAUTHORIZED;    // 그 외 인증 실패 (토큰 없이 접근 등)
        }

        setResponse(response, errorCode);
    }

    private void setResponse(HttpServletResponse response, ErrorResponseCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value()); // HttpStatus -> int
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Object> body = ApiResponse.error(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
