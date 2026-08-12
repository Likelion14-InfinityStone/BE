package com.medipass.server.domain.regulation.source;

import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.regulation.web.dto.RequirementTemplateRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 일본(JP) 서류 템플릿 Source
 * 후생노동성 안내 기준, 소량·큐레이션 데이터라 코드 안에 직접 관리
 * 분류: N(마약)·SRM(각성제원료)은 허가 필요, P(향정신성)는 한도 초과 시 진단서
 */
@Component
public class JpRequirementSource implements CountryRequirementSource {

    private static final String COUNTRY_CODE = "JP";

    @Override
    public String countryCode() {
        return COUNTRY_CODE;
    }

    @Override
    public List<RequirementTemplateRecord> load() {
        List<RequirementTemplateRecord> list = new ArrayList<>();

        // N — 마약 (수입/수출 허가 필요)
        list.add(t("N", RequirementKind.ACTION, "마약 수입 허가 신청서",
                "일본 입국 시 필요. 관할 지방후생국 마약단속부(NCD)에 최소 14일 전 제출 (이메일/FAX/우편)"));
        list.add(t("N", RequirementKind.ACTION, "마약 수출 허가 신청서",
                "일본 출국 시(잔여 약 반출) 필요. 불필요하면 이메일에 그 취지를 기재"));
        list.add(t("N", RequirementKind.UPLOAD, "영문 의사 진단서",
                "이름·주소·구체적 복용 사유·약 목록(용량·함량)·처방의 서명·발급일(3개월 이내) 포함"));
        list.add(t("N", RequirementKind.UPLOAD, "약 포장 사진 또는 관련 서류",
                "약 이름·함량 확인용. JPEG/PDF/Word 로 제출 (HEIC 불가)"));

        // SRM — 각성제 원료 (수입/수출 허가 필요)
        list.add(t("SRM", RequirementKind.ACTION, "각성제 원료 수입 허가 신청서",
                "일본 입국 시 필요. 관할 NCD 에 최소 14일 전 제출"));
        list.add(t("SRM", RequirementKind.ACTION, "각성제 원료 수출 허가 신청서",
                "일본 출국 시(잔여 약 반출) 필요. 불필요하면 이메일에 그 취지를 기재"));
        list.add(t("SRM", RequirementKind.UPLOAD, "영문 의사 진단서",
                "이름·주소·구체적 복용 사유·약 목록(용량·함량)·처방의 서명·발급일(3개월 이내) 포함"));
        list.add(t("SRM", RequirementKind.UPLOAD, "약 포장 사진 또는 관련 서류",
                "약 이름·함량 확인용. JPEG/PDF/Word 로 제출 (HEIC 불가)"));

        // P — 향정신성 (한도 이하 서류 불필요, 초과·주사제면 진단서)
        list.add(t("P", RequirementKind.UPLOAD, "영문 의사 진단서 (한도 초과·주사제 시)",
                "허용 한도 이하면 불필요. 한도 초과 또는 주사제일 때만 필요. 이름·병명·복용 필요성·약 목록(용량·함량) 포함"));
        list.add(t("P", RequirementKind.ACTION, "1개월분 초과 시 기관 문의", "mailto:yakkan@mhlw.go.jp",
                "1개월분을 초과해 반입하려면 사전에 후생노동성(yakkan@mhlw.go.jp)으로 문의 필요"));

        return list;
    }

    private RequirementTemplateRecord t(String category, RequirementKind kind,
                                        String label, String description) {
        return t(category, kind, label, null, description);
    }

    private RequirementTemplateRecord t(String category, RequirementKind kind,
                                        String label, String formUrl, String description) {
        return new RequirementTemplateRecord(category, kind, label, formUrl, description);
    }
}
