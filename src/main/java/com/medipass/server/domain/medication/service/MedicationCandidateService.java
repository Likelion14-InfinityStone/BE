package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.web.dto.MedicationCandidateRecord;
import com.medipass.server.domain.medication.web.dto.MedicationCandidateSearchRes;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.mfds.client.MfdsClient;
import com.medipass.server.global.mfds.dto.MfdsProductItem;
import com.medipass.server.global.mfds.dto.MfdsProductSearchResult;
import com.medipass.server.global.mfds.exception.MfdsErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MedicationCandidateService {

    private static final Pattern TRAILING_DOSAGE_PATTERN = Pattern.compile(
            "(?i)\\s*\\d+(?:[.,]\\d+)?\\s*"
                    + "(?:mg|㎎|밀리그램|밀리그람|g|그램|mcg|μg|㎍|마이크로그램|마이크로그람|ml|㎖|밀리리터)\\s*$"
    );

    private final MfdsClient mfdsClient;

    // OCR 최초 자동 매칭과 사용자 제품명 수정 후 재매칭에서 공통으로 사용하는 내부 서비스다
    public MedicationCandidateSearchRes search(String ocrProductText) {
        String searchKeyword = normalizeSearchKeyword(ocrProductText);
        MfdsProductSearchResult result = mfdsClient.searchByProductName(searchKeyword);

        /*
         * 식약처 제품명 검색은 OCR 문자열 끝에 함량이 붙으면 결과가 없을 수 있다.
         * 1차 검색이 실패한 경우에만 끝의 '숫자 + 용량 단위'를 제거하여 한 번 재검색한다.
         * '타이레놀8시간이알서방정'처럼 제품명 자체에 포함된 숫자는 제거하지 않는다.
         */
        if (result.items().isEmpty()) {
            String fallbackKeyword = removeTrailingDosage(searchKeyword);
            if (!fallbackKeyword.equals(searchKeyword)) {
                searchKeyword = fallbackKeyword;
                result = mfdsClient.searchByProductName(searchKeyword);
            }
        }

        // 직접 입력 화면에서 정확한 함량·제형을 선택할 수 있도록 검색 후보 전체를 반환한다.
        var candidates = result.items().stream()
                .map(this::toCandidateResponse)
                .toList();

        return new MedicationCandidateSearchRes(searchKeyword, result.totalCount(), candidates);
    }

    private String removeTrailingDosage(String searchKeyword) {
        return TRAILING_DOSAGE_PATTERN.matcher(searchKeyword).replaceFirst("").trim();
    }

    private MedicationCandidateRecord toCandidateResponse(MfdsProductItem item) {
        // 자동 매칭 단계에서는 제품 식별에 필요한 최소 정보만 클라이언트에 반환
        return new MedicationCandidateRecord(
                item.itemSeq(),
                item.itemName(),
                item.itemEngName()
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
