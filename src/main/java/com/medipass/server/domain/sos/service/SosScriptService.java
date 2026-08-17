package com.medipass.server.domain.sos.service;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.sos.exception.SosErrorCode;
import com.medipass.server.domain.sos.web.dto.SosScriptReq;
import com.medipass.server.domain.sos.web.dto.SosScriptRes;
import com.medipass.server.domain.trip.entity.Trip;
import com.medipass.server.domain.trip.exception.TripAccessDeniedException;
import com.medipass.server.domain.trip.exception.TripNotFoundException;
import com.medipass.server.domain.trip.repository.TripRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.openai.client.OpenAiClient;
import com.medipass.server.global.openai.prompt.SosScriptTranslationPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 현지어 설명문 번역
 *
 * 원문은 프론트가 상황별 템플릿으로 완성해서 보내고, 서버는 도착국 언어로 번역만 한다.
 *
 * 클래스에 @Transactional 을 걸지 않은 것은 의도적이다.
 * OpenAI 호출이 수 초 걸릴 수 있는데 그동안 커넥션을 붙잡고 있으면 안 된다.
 * 여행 조회는 리포지토리 자체 트랜잭션으로 끝나고, 번역은 트랜잭션 밖에서 이뤄진다.
 */
@Service
@RequiredArgsConstructor
public class SosScriptService {

    // 도착국 언어코드(country.local_lang) → 화면 표기명. 번역 프롬프트에도 이 이름을 쓴다
    private static final Map<String, String> SUPPORTED_LANGUAGES = Map.of("ja", "일본어");

    private final TripRepository tripRepository;
    private final OpenAiClient openAiClient;

    public SosScriptRes translate(Long userId, SosScriptReq request) {
        Trip trip = loadOwnedTrip(userId, request.tripId());
        Country destination = trip.getDestinationCountry();

        String targetLanguage = destination.getLocalLang();
        String targetLanguageLabel = targetLanguage == null ? null : SUPPORTED_LANGUAGES.get(targetLanguage);
        if (targetLanguageLabel == null) {
            throw new BaseException(SosErrorCode.SCRIPT_LANGUAGE_NOT_SUPPORTED);
        }

        String translated = openAiClient.chat(
                SosScriptTranslationPrompt.SYSTEM,
                SosScriptTranslationPrompt.user(targetLanguageLabel, request.text().strip()));

        return new SosScriptRes(targetLanguage, targetLanguageLabel, translated);
    }

    private Trip loadOwnedTrip(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(TripNotFoundException::new);
        if (!trip.getUser().getId().equals(userId)) {
            throw new TripAccessDeniedException();
        }
        return trip;
    }
}
