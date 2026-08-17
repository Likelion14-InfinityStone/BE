package com.medipass.server.global.mofa.client;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.mofa.config.MofaProperties;
import com.medipass.server.global.mofa.dto.MofaMissionItem;
import com.medipass.server.global.mofa.exception.MofaErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MofaClientImpl implements MofaClient {

    private static final String LIST_OPERATION = "/getEmbassyList2"; // 외교부 국가·지역별 재외공관 정보
    private static final int LIST_LIMIT = 100;                       // 국가당 10건 안팎이라 한 번에 다 받는다
    private static final String COUNTRY_CONDITION = "cond[country_iso_alp2::EQ]";
    private static final String RESULT_CODE_OK = "0";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final MofaProperties properties;

    // 국가 코드로 재외공관 목록 조회
    @Override
    public List<MofaMissionItem> findMissionsByCountry(String countryCode) {
        validateConfiguration(); // API 호출 전에 baseUrl과 serviceKey 설정 여부 확인

        try {
            String response = restClientBuilder
                    .baseUrl(properties.baseUrl())
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(LIST_OPERATION)
                            .queryParam("serviceKey", "{serviceKey}") // 공공데이터포털 인증키
                            .queryParam("returnType", "JSON")         // MFDS 는 type, 외교부는 returnType 을 쓴다
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", LIST_LIMIT)
                            .queryParam(COUNTRY_CONDITION, "{countryCode}")
                            .build(properties.serviceKey(), countryCode.toUpperCase()))
                    .retrieve()
                    .body(String.class);

            return parseResponse(response);
        } catch (RestClientException | JacksonException e) {
            log.warn("외교부 API 호출 또는 응답 처리 실패: {}", e.getMessage());
            throw new BaseException(MofaErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    // 외교부 응답 파싱 — response.body.items.item 배열
    private List<MofaMissionItem> parseResponse(String response) throws JacksonException {
        if (response == null || response.isBlank()) {
            throw new BaseException(MofaErrorCode.SERVICE_UNAVAILABLE);
        }

        JsonNode parsed = objectMapper.readTree(response);
        JsonNode root = parsed.has("response") ? parsed.path("response") : parsed;
        JsonNode header = root.path("header");

        if (isErrorResponse(header)) {
            log.warn("외교부 API 오류 응답: resultCode={}, resultMsg={}",
                    header.path("resultCode").asText(), header.path("resultMsg").asText());
            throw new BaseException(MofaErrorCode.SERVICE_UNAVAILABLE);
        }

        JsonNode itemsNode = root.path("body").path("items");
        if (itemsNode.has("item")) {
            itemsNode = itemsNode.path("item");
        }

        List<MofaMissionItem> items = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                items.add(objectMapper.treeToValue(itemNode, MofaMissionItem.class));
            }
        } else if (itemsNode.isObject()) { // 공관이 하나뿐인 국가면 객체 하나로 올 수 있음
            items.add(objectMapper.treeToValue(itemsNode, MofaMissionItem.class));
        }

        return List.copyOf(items);
    }

    // 외교부 응답 상태 확인 (정상이면 resultCode = "0")
    private boolean isErrorResponse(JsonNode header) {
        String resultCode = header.path("resultCode").asText();
        return !resultCode.isBlank() && !RESULT_CODE_OK.equals(resultCode);
    }

    // 환경설정 검증
    private void validateConfiguration() {
        if (properties.baseUrl() == null || properties.baseUrl().isBlank()
                || properties.serviceKey() == null || properties.serviceKey().isBlank()) {
            throw new BaseException(MofaErrorCode.NOT_CONFIGURED);
        }
    }
}
