package com.medipass.server.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청마다 추적용 ID를 발급해 MDC(로그)와 응답 헤더(X-Request-Id)에 넣는다. 에러 응답의 requestId 도 여기서 넣은 MDC 값을 사용한다.
 * 프론트가 보낸 헤더는 신뢰하지 않고 서버가 항상 새로 생성한다. (위변조 방지)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID = "requestId";
    public static final String HEADER = "X-Request-Id";

    /** UUID 앞 8자리만 사용 (로그에서 읽기 쉽게) */
    private static final int ID_LENGTH = 8;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException
    {
        String requestId = UUID.randomUUID().toString().substring(0, ID_LENGTH);
        MDC.put(REQUEST_ID, requestId);
        response.setHeader(HEADER, requestId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long took = System.currentTimeMillis() - start;
            // 성공/실패 모든 요청을 한 줄씩 기록 (method, URI, 상태코드, 소요시간)
            log.info("{} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), took);

            MDC.remove(REQUEST_ID); // 스레드 재사용 시 값이 남지 않도록 정리
        }
    }
}
