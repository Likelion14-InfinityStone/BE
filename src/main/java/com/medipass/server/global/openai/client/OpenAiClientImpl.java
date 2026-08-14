package com.medipass.server.global.openai.client;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.openai.config.OpenAiProperties;
import com.medipass.server.global.openai.exception.OpenAiErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClientImpl implements OpenAiClient {

    private static final String CHAT_PATH = "/chat/completions";
    private static final double TEMPERATURE = 0.0; // 근거 톤·형식 일관성 최우선 (군더더기 최소화)

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties properties;

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        validateConfiguration();

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", properties.model(),
                    "temperature", TEMPERATURE,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt))));

            String response = restClientBuilder
                    .baseUrl(properties.baseUrl())
                    .build()
                    .post()
                    .uri(CHAT_PATH)
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractContent(response);
        } catch (RestClientException | JacksonException e) {
            log.warn("OpenAI 호출 또는 응답 처리 실패: {}", e.getMessage());
            throw new BaseException(OpenAiErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    // choices[0].message.content 추출
    private String extractContent(String response) throws JacksonException {
        if (response == null || response.isBlank()) {
            throw new BaseException(OpenAiErrorCode.SERVICE_UNAVAILABLE);
        }
        JsonNode content = objectMapper.readTree(response)
                .path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asString().isBlank()) {
            throw new BaseException(OpenAiErrorCode.SERVICE_UNAVAILABLE);
        }
        return content.asString().strip();
    }

    private void validateConfiguration() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()
                || properties.baseUrl() == null || properties.baseUrl().isBlank()
                || properties.model() == null || properties.model().isBlank()) {
            throw new BaseException(OpenAiErrorCode.NOT_CONFIGURED);
        }
    }
}
