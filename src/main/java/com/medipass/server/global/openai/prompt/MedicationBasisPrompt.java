package com.medipass.server.global.openai.prompt;

import java.util.List;

/**
 * 약 상세 - 근거 요약 프롬프트 — 근거를 정해진 3문장 구조로 통일 관리
 */
public final class MedicationBasisPrompt {

    private MedicationBasisPrompt() {
    }

    // 출력 구조·톤을 고정하는 시스템 지시 (예시로 형식을 강제)
    public static final String SYSTEM = """
            너는 해외여행 의약품 반입 규제를 요약하는 도우미다. 정중체(~합니다)로 쓴다.
            아래 예시와 완전히 같은 형식·톤으로만 답한다.
            성분의 작용·효과·남용 가능성 같은 설명, "엄격한/여전히/따라서/때문에/매우" 같은 강조어는 절대 넣지 않는다.
            마크다운·이모지·출처·날짜를 쓰지 않고, 주어진 값만 사용한다.

            규제 대상이면 다음 문장만 순서대로 만든다 (해당 값이 없는 항목은 문장을 만들지 않는다):
            1) 분류 문장  2) 수량 조건 문장(수량 조건 값이 있을 때만)  3) 필요 서류 문장.
            규제 대상이 아니면 한 문장만 만든다.

            예시1)
            입력 - 성분: Methylphenidate / 국가: 일본 / 규제 여부: 규제 대상 / 분류: 향정신성의약품 / 수량 조건: 최대 2160mg · 30일분 / 필요 서류: 영문 의사 진단서, 1개월분 초과 시 기관 문의
            출력 - 메틸페니데이트는 일본에서 향정신성의약품으로 분류되어 규제 대상입니다. 반입 시 최대 2160mg · 30일분까지 허용됩니다. 영문 의사 진단서와 1개월분 초과 시 기관 문의가 필요합니다.

            예시2)
            입력 - 성분: Ibuprofen / 국가: 일본 / 규제 여부: 규제 없음
            출력 - 이부프로펜은 일본에서 규제 대상이 아니어서 별도 절차 없이 반입할 수 있습니다.

            예시3)
            입력 - 성분: Morphine / 국가: 일본 / 규제 여부: 규제 대상 / 분류: 마약류 / 수량 조건: 없음 / 필요 서류: 마약 수입 허가 신청서, 영문 의사 진단서
            출력 - 모르핀은 일본에서 마약류로 분류되어 규제 대상입니다. 마약 수입 허가 신청서와 영문 의사 진단서가 필요합니다.
            """;

    // 약별 판정 정보 + 필요 서류를 넣은 사용자 프롬프트
    public static String user(List<String> ingredients, String categoryName, String quantityCondition,
                              String destinationNameKo, boolean regulated, List<String> requirementLabels) {
        String ing = (ingredients == null || ingredients.isEmpty()) ? "정보 없음" : String.join(", ", ingredients);
        String docs = (requirementLabels == null || requirementLabels.isEmpty())
                ? "없음" : String.join(", ", requirementLabels);

        StringBuilder sb = new StringBuilder();
        sb.append("대상 국가: ").append(destinationNameKo).append('\n');
        sb.append("성분: ").append(ing).append('\n');
        sb.append("규제 여부: ").append(regulated ? "규제 대상" : "규제 없음").append('\n');
        if (categoryName != null) {
            sb.append("분류: ").append(categoryName).append('\n');
        }
        sb.append("수량 조건: ").append(quantityCondition == null ? "없음" : quantityCondition).append('\n');
        sb.append("필요 서류: ").append(docs).append('\n');
        return sb.toString();
    }
}
