package com.medipass.server.global.openai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI 설정 — 실제 api-key 는 application-secret.properties(또는 환경변수 OPENAI_API_KEY)에서 주입
 */
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        String baseUrl,
        String apiKey,
        String model
) {
}
