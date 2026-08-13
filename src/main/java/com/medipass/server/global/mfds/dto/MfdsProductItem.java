package com.medipass.server.global.mfds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MfdsProductItem(
        // 약 도메인은 필요한 값만 골라 응답하고, 전체 상세정보는 여행 규정 분석에서 재사용한다.
        @JsonProperty("ITEM_SEQ") String itemSeq,
        @JsonProperty("ITEM_NAME") String itemName,
        @JsonProperty("ITEM_ENG_NAME") String itemEngName,
        @JsonProperty("EDI_CODE") String ediCode,
        @JsonProperty("ENTP_NAME") String manufacturer,
        @JsonProperty("ETC_OTC_CODE") String etcOtcCode,
        @JsonProperty("NARCOTIC_KIND_CODE") String narcoticKindCode,
        @JsonProperty("ATC_CODE") String atcCode,
        @JsonProperty("MAIN_ITEM_INGR") String mainItemIngredient,
        @JsonProperty("MAIN_INGR_ENG") String mainIngredientEnglish,
        @JsonProperty("MATERIAL_NAME") String materialName
) {
}
