package com.medipass.server.domain.regulation.service;

import com.medipass.server.domain.regulation.entity.IngredientRegulation;
import com.medipass.server.domain.regulation.entity.PreparationLevel;
import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import com.medipass.server.domain.regulation.repository.IngredientRegulationRepository;
import com.medipass.server.domain.regulation.repository.RequirementTemplateRepository;
import com.medipass.server.domain.regulation.web.dto.JudgeReq;
import com.medipass.server.domain.regulation.web.dto.JudgeRes;
import com.medipass.server.domain.regulation.web.dto.JudgeRes.IngredientResult;
import com.medipass.server.domain.regulation.web.dto.JudgeRes.RequirementView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 규제 판정 엔진
 * (국가 + 성분들) → 성분별 신호등·한도·필요서류. 우리 데이터만으로 완결되며 식약처/여행저장과 무관
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegulationJudgeService {

    private final IngredientRegulationRepository regulationRepository;
    private final RequirementTemplateRepository templateRepository;

    public JudgeRes judge(JudgeReq request) {
        String country = request.country().toUpperCase();
        List<IngredientResult> results = request.ingredients().stream()
                .map(in -> judgeOne(country, in.inn(), in.amountMg()))
                .toList();
        return new JudgeRes(country, results);
    }

    private IngredientResult judgeOne(String country, String inn, BigDecimal amountMg) {
        Optional<IngredientRegulation> found =
                regulationRepository.findByCountry_CodeAndSubstanceName(country, inn);

        // 규제 목록에 없음 → 그냥 반입 가능
        if (found.isEmpty()) {
            return new IngredientResult(inn, false, PreparationLevel.ALLOWED,
                    null, null, false, Collections.emptyList());
        }

        IngredientRegulation reg = found.get();
        boolean overLimit = amountMg != null && reg.getLimitMg() != null
                && amountMg.compareTo(reg.getLimitMg()) > 0;
        PreparationLevel level = effectiveLevel(reg, amountMg, overLimit);

        // 준비가 필요한 경우에만 서류 목록을 붙임
        List<RequirementView> requirements = (level == PreparationLevel.PREP_REQUIRED)
                ? loadRequirements(country, reg.getCategoryCode())
                : Collections.emptyList();

        return new IngredientResult(inn, true, level,
                reg.getCategoryCode(), reg.getLimitMg(), overLimit, requirements);
    }

    /**
     * 저장된 등급을 소지량 기준으로 보정
     * - 금지(NOT_ALLOWED)는 그대로
     * - 한도가 있는 향정신성(P)은 소지량이 한도 이하면 ALLOWED, 초과/미상이면 PREP_REQUIRED
     * - 그 외는 저장된 등급 그대로 (PREP_REQUIRED)
     */
    private PreparationLevel effectiveLevel(IngredientRegulation reg, BigDecimal amountMg, boolean overLimit) {
        if (reg.getPreparationLevel() == PreparationLevel.NOT_ALLOWED) {
            return PreparationLevel.NOT_ALLOWED;
        }
        if (reg.getLimitMg() != null) {
            boolean underLimit = amountMg != null && !overLimit;
            return underLimit ? PreparationLevel.ALLOWED : PreparationLevel.PREP_REQUIRED;
        }
        return reg.getPreparationLevel();
    }

    private List<RequirementView> loadRequirements(String country, String categoryCode) {
        List<RequirementTemplate> templates =
                templateRepository.findByCountry_CodeAndCategoryCode(country, categoryCode);
        return templates.stream()
                .map(t -> new RequirementView(t.getKind(), t.getLabel(), t.getFormUrl(), t.getDescription()))
                .toList();
    }
}
