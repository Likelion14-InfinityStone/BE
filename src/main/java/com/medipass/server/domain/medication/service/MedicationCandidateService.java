package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.dto.response.MedicationCandidateResponse;
import com.medipass.server.domain.medication.dto.response.MedicationCandidateSearchResponse;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.mfds.client.MfdsClient;
import com.medipass.server.global.mfds.dto.MfdsProductItem;
import com.medipass.server.global.mfds.dto.MfdsProductSearchResult;
import com.medipass.server.global.mfds.exception.MfdsErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicationCandidateService {

    private final MfdsClient mfdsClient;

    // OCR 최초 자동 매칭과 사용자 제품명 수정 후 재매칭에서 공통으로 사용하는 내부 서비스다
    public MedicationCandidateSearchResponse search(String ocrProductText) {
        String searchKeyword = normalizeSearchKeyword(ocrProductText);
        MfdsProductSearchResult result = mfdsClient.searchByProductName(searchKeyword);

        /*
         * MVP에서는 별도의 후보 선택 화면을 제공하지 않는다.
         * 식약처 검색 결과가 한 건이면 해당 제품을 사용하고, 여러 건이면
         * 식약처가 반환한 순서를 신뢰하여 첫 번째 제품을 자동 매칭한다.
         * 검색 결과가 없을 때는 matchedProduct를 null로 내려 프론트에서
         * 직접 입력 또는 재촬영 흐름으로 처리할 수 있도록 한다.
         */
        MedicationCandidateResponse matchedProduct = result.items().stream()
                .findFirst()
                .map(this::toCandidateResponse)
                .orElse(null);

        return new MedicationCandidateSearchResponse(searchKeyword, result.totalCount(), matchedProduct);
    }

    private MedicationCandidateResponse toCandidateResponse(MfdsProductItem item) {
        // 자동 매칭 단계에서는 제품 식별에 필요한 최소 정보만 클라이언트에 반환
        return new MedicationCandidateResponse(
                item.itemSeq(),
                item.itemName(),
                item.itemEngName(),
                item.ediCode()
        );
    }

    private String normalizeSearchKeyword(String ocrProductText) {
        if (ocrProductText == null || ocrProductText.isBlank()) {
            throw new BaseException(MfdsErrorCode.EMPTY_QUERY);
        }

        String keyword = ocrProductText.trim();
        int openingBracket = firstBracketIndex(keyword);
        if (openingBracket >= 0) {
            keyword = keyword.substring(0, openingBracket);
        }
        keyword = keyword.replaceFirst("[.,;:!?\\-_/·…]+$", "").trim();

        if (keyword.isBlank()) {
            throw new BaseException(MfdsErrorCode.EMPTY_QUERY);
        }
        return keyword;
    }

    private int firstBracketIndex(String value) {
        int index = -1;
        for (char bracket : new char[]{'(', '[', '{'}) {
            int current = value.indexOf(bracket);
            if (current >= 0 && (index < 0 || current < index)) {
                index = current;
            }
        }
        return index;
    }
}
