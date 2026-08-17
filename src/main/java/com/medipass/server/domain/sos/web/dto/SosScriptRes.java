package com.medipass.server.domain.sos.web.dto;

/**
 * 현지어 설명문 번역 결과
 * 한국어 원문은 프론트가 이미 갖고 있으므로 돌려주지 않는다
 */
public record SosScriptRes(
        String targetLanguage,       // ja
        String targetLanguageLabel,  // 일본어 — 카드 상단 "한국어 → 일본어" 표기용
        String translatedText        // 원문과 같은 줄 수, 줄바꿈(\n) 포함
) {
}
