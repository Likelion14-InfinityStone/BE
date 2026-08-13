package com.medipass.server.global.mfds.client;

import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.mfds.config.MfdsProperties;
import com.medipass.server.global.mfds.dto.MfdsProductItem;
import com.medipass.server.global.mfds.dto.MfdsProductSearchResult;
import com.medipass.server.global.mfds.exception.MfdsErrorCode;
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
public class MfdsClientImpl implements MfdsClient {


    private static final String SEARCH_OPERATION = "/getDrugPrdtPrmsnDtlInq06"; // 식약처 의약품 허가 상세정보 조회 API
    private static final int SEARCH_LIMIT = 100; // 한 번의 검색에서 조회할 최대 제품 수

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final MfdsProperties properties;

    // 제품명으로 식약처 의약품 검색
    @Override
    public MfdsProductSearchResult searchByProductName(String productName) {
        validateConfiguration(); // API 호출 전에 baseUrl과 serviceKey 설정 여부 확인

        try {
            // 식약처 API에 제품명을 검색 조건으로 GET 요청
            String response = restClientBuilder
                    .baseUrl(properties.baseUrl())
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_OPERATION)
                            .queryParam("serviceKey", "{serviceKey}") // 공공데이터포털 인증키
                            .queryParam("type", "json") // JSON 형식으로 응답
                            .queryParam("pageNo", 1) // 첫 번째 페이지 조회
                            .queryParam("numOfRows", SEARCH_LIMIT) // 최대 조회 건수
                            .queryParam("item_name", "{itemName}") // 제품명 검색 조건
                            .build(properties.serviceKey(), productName))
                    .retrieve()
                    .body(String.class);

            return parseResponse(response);
        } catch (RestClientException | JacksonException e) {
            log.warn("식약처 API 호출 또는 응답 처리 실패: {}", e.getMessage());
            throw new BaseException(MfdsErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    // 식약처 응답 파싱
    private MfdsProductSearchResult parseResponse(String response) throws JacksonException {
        if (response == null || response.isBlank()) {
            throw new BaseException(MfdsErrorCode.SERVICE_UNAVAILABLE);
        }

        JsonNode parsed = objectMapper.readTree(response);
        JsonNode root = parsed.has("response") ? parsed.path("response") : parsed;
        JsonNode header = root.path("header");

        if (isErrorResponse(header)) {
            log.warn("식약처 API 오류 응답: resultCode={}, resultMsg={}",
                    header.path("resultCode").asText(), header.path("resultMsg").asText());
            throw new BaseException(MfdsErrorCode.SERVICE_UNAVAILABLE);
        }

        JsonNode body = root.path("body"); // 실제 검색 결과가 들어있는 body 추출
        int totalCount = body.path("totalCount").asInt(0); // 전체 검색 결과 수
        JsonNode itemsNode = body.path("items"); // 제품 목록 추출
        if (itemsNode.has("item")) {
            itemsNode = itemsNode.path("item");
        }

        List<MfdsProductItem> items = new ArrayList<>();
        if (itemsNode.isArray()) { // 검색 결과가 여러 건인 경우
            for (JsonNode itemNode : itemsNode) {
                items.add(objectMapper.treeToValue(itemNode, MfdsProductItem.class));
            }
        } else if (itemsNode.isObject()) { // 검색 결과가 한 건만 객체 형태로 오는 경우
            items.add(objectMapper.treeToValue(itemsNode, MfdsProductItem.class));
        }

        return new MfdsProductSearchResult(totalCount, List.copyOf(items));
    }

    // 식약처 응답 상태 확인
    private boolean isErrorResponse(JsonNode header) {
        String resultCode = header.path("resultCode").asText();
        return !resultCode.isBlank() // 정상 응답 코드가 아니면 오류로 판단
                && !"00".equals(resultCode)
                && !"NORMAL_SERVICE".equalsIgnoreCase(resultCode);
    }

    // 환경설정 검증
    private void validateConfiguration() {
        // baseUrl 또는 serviceKey가 없으면 API를 호출하지 않고 바로 오류 처리
        if (properties.baseUrl() == null || properties.baseUrl().isBlank()
                || properties.serviceKey() == null || properties.serviceKey().isBlank()) {
            throw new BaseException(MfdsErrorCode.NOT_CONFIGURED);
        }
    }
}
