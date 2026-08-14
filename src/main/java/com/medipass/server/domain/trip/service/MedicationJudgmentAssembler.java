package com.medipass.server.domain.trip.service;

import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.regulation.entity.PreparationLevel;
import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import com.medipass.server.domain.regulation.repository.RequirementTemplateRepository;
import com.medipass.server.domain.regulation.web.dto.JudgeRes;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 판정 결과 조립 — judge 성분별 결과 + 일수를 화면 표시용 약별 판정으로 가공
 * 중량(mg) 판정과 기간(30일 초과) 판정을 합치고, 분류명·수량조건·사유별 서류를 붙인다
 */
@Component
@RequiredArgsConstructor
public class MedicationJudgmentAssembler {

    private final RequirementTemplateRepository templateRepository;

    // 일반 반입 기준 (처방약 1개월분) — 규제 테이블에 없어 상수로 관리
    private static final int PERSONAL_IMPORT_DAYS = 30;

    // 분류 코드
    private static final String CATEGORY_NARCOTIC = "N";
    private static final String CATEGORY_PSYCHOTROPIC = "P";
    private static final String CATEGORY_STIMULANT_MATERIAL = "SRM";

    // judge 결과 → 약 하나의 판정 결과(신호등·분류·수량조건·필요서류)
    public TripAnalyzeRes.MedicationResult assemble(
            Medication medication, String countryCode, int carryDays, JudgeRes judged) {
        boolean regulated = judged.results().stream().anyMatch(JudgeRes.IngredientResult::regulated);
        String category = representativeCategory(judged.results());

        // 중량(mg) 판정 + 기간(30일) 초과 판정을 합쳐 최종 신호등 산출
        boolean daysOver = regulated && carryDays > PERSONAL_IMPORT_DAYS;
        PreparationLevel level = finalLevel(aggregateLevel(judged.results()), daysOver);

        String categoryName = categoryName(category);
        String quantityCondition = quantityCondition(category, judged.results());
        List<TripAnalyzeRes.RequirementItem> requirements = (level == PreparationLevel.PREP_REQUIRED)
                ? buildRequirements(countryCode, judged.results(), daysOver)
                : List.of();

        return new TripAnalyzeRes.MedicationResult(
                medication.getId(), medication.getProductKoName(), level, regulated,
                category, categoryName, quantityCondition, requirements);
    }

    // 성분별 신호등 → 약 하나의 신호등 (가장 나쁜 것)
    private PreparationLevel aggregateLevel(List<JudgeRes.IngredientResult> results) {
        boolean anyPrep = false;
        for (JudgeRes.IngredientResult r : results) {
            if (r.level() == PreparationLevel.NOT_ALLOWED) {
                return PreparationLevel.NOT_ALLOWED;
            }
            if (r.level() == PreparationLevel.PREP_REQUIRED) {
                anyPrep = true;
            }
        }
        return anyPrep ? PreparationLevel.PREP_REQUIRED : PreparationLevel.ALLOWED;
    }

    // 대표 분류 (규제된 성분 중 첫 분류) — 화면 "통제 성분 여부 (마약류)" 표기용
    private String representativeCategory(List<JudgeRes.IngredientResult> results) {
        return results.stream()
                .filter(r -> r.regulated() && r.categoryCode() != null)
                .map(JudgeRes.IngredientResult::categoryCode)
                .findFirst().orElse(null);
    }

    // mg 기반 신호등에 기간(30일) 초과를 반영해 최종 신호등 산출
    private PreparationLevel finalLevel(PreparationLevel mgLevel, boolean daysOver) {
        if (mgLevel == PreparationLevel.NOT_ALLOWED) {
            return PreparationLevel.NOT_ALLOWED;
        }
        if (mgLevel == PreparationLevel.PREP_REQUIRED || daysOver) {
            return PreparationLevel.PREP_REQUIRED;
        }
        return PreparationLevel.ALLOWED;
    }

    // 분류 표시명 (화면 "통제 성분 여부" 표기용)
    private String categoryName(String category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case CATEGORY_NARCOTIC -> "마약류";
            case CATEGORY_PSYCHOTROPIC -> "향정신성의약품";
            case CATEGORY_STIMULANT_MATERIAL -> "각성제 원료";
            default -> category;
        };
    }

    // 수량 조건 — 향정(mg 한도)은 "최대 {mg}mg · 30일분", 허가 분류(N·SRM)는 허가 안내, 규제 없음은 null
    private String quantityCondition(String category, List<JudgeRes.IngredientResult> results) {
        BigDecimal limitMg = results.stream()
                .filter(r -> r.regulated() && r.limitMg() != null)
                .map(JudgeRes.IngredientResult::limitMg)
                .findFirst().orElse(null);
        if (limitMg != null) {
            return "최대 " + limitMg.stripTrailingZeros().toPlainString() + "mg · " + PERSONAL_IMPORT_DAYS + "일분";
        }
        if (category != null) {
            return "반입 시 허가 필요";
        }
        return null;
    }

    // 사유별 서류 구성 — N·SRM 은 항상 전체, P 는 중량 초과→진단서(UPLOAD)·기간 초과→문의(ACTION)
    private List<TripAnalyzeRes.RequirementItem> buildRequirements(
            String countryCode, List<JudgeRes.IngredientResult> results, boolean daysOver) {
        Set<String> categories = new HashSet<>();
        for (JudgeRes.IngredientResult r : results) {
            if (r.regulated() && r.categoryCode() != null) {
                categories.add(r.categoryCode());
            }
        }

        List<TripAnalyzeRes.RequirementItem> items = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>();
        for (String category : categories) {
            boolean isPsychotropic = CATEGORY_PSYCHOTROPIC.equals(category);
            boolean mgOver = results.stream()
                    .anyMatch(r -> category.equals(r.categoryCode()) && r.overLimit());
            for (RequirementTemplate t : templateRepository.findByCountry_CodeAndCategoryCode(countryCode, category)) {
                if (includeRequirement(isPsychotropic, t.getKind(), mgOver, daysOver) && addedIds.add(t.getId())) {
                    items.add(new TripAnalyzeRes.RequirementItem(
                            t.getId(), t.getKind(), t.getLabel(), t.getFormUrl(), t.getDescription()));
                }
            }
        }
        return items;
    }

    // P 는 사유에 맞는 서류만, 그 외 분류(N·SRM)는 항상 필요
    private boolean includeRequirement(boolean isPsychotropic, RequirementKind kind, boolean mgOver, boolean daysOver) {
        if (!isPsychotropic) {
            return true;
        }
        if (kind == RequirementKind.UPLOAD) {
            return mgOver;   // 중량 한도 초과 → 영문 진단서
        }
        if (kind == RequirementKind.ACTION) {
            return daysOver; // 1개월분 초과 → 기관 문의
        }
        return false;
    }
}
