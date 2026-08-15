package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.global.openai.client.OpenAiClient;
import com.medipass.server.global.openai.prompt.IssuerTranslationPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssuerTranslationService {

    private final OpenAiClient openAiClient;

    // 저장된 영문명이 있으면 재사용하고, 없을 때만 최초 번역한다.
    public String getOrTranslate(Medication medication) {
        String issuer = medication.getIssuer();
        if (issuer == null || issuer.isBlank()) {
            return null;
        }

        // 이전에 번역한 결과는 외부 API를 다시 호출하지 않고 반환한다.
        if (medication.getIssuerEn() != null && !medication.getIssuerEn().isBlank()) {
            return medication.getIssuerEn();
        }

        // 한글이 없는 기관명은 이미 영문으로 보고 원문을 사용한다.
        if (!containsHangul(issuer)) {
            return issuer;
        }

        // 최초 영문 조회 시 번역하고 엔티티에 저장해 이후 요청에서 재사용한다.
        String translated = openAiClient.chat(
                IssuerTranslationPrompt.SYSTEM,
                IssuerTranslationPrompt.user(issuer)
        );
        medication.updateIssuerEn(translated);
        return translated;
    }

    // 완성형 한글 음절이 포함됐는지 확인한다.
    private boolean containsHangul(String value) {
        return value.codePoints().anyMatch(codePoint ->
                codePoint >= 0xAC00 && codePoint <= 0xD7A3
        );
    }
}
