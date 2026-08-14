package com.medipass.server.domain.trip.service;

import com.medipass.server.domain.medication.entity.MfdsProduct;
import com.medipass.server.domain.regulation.entity.IngredientRegulation;
import com.medipass.server.domain.regulation.repository.IngredientRegulationRepository;
import com.medipass.server.domain.trip.entity.TripMedication;
import com.medipass.server.domain.trip.repository.ChecklistItemRepository;
import com.medipass.server.domain.trip.web.dto.TripMedicationBasisRes;
import com.medipass.server.global.openai.client.OpenAiClient;
import com.medipass.server.global.openai.prompt.MedicationBasisPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 약 상세 - 근거 생성 — 저장된 근거가 없으면 OpenAI로 만들어 trip_medication 에 저장(lazy)
 * 출처·확인일은 AI가 아니라 규제 데이터(TSV)의 sourceUrl·verifiedDate 에서 가져온다
 */
@Service
@RequiredArgsConstructor
public class MedicationBasisService {

    private final OpenAiClient openAiClient;
    private final IngredientRegulationRepository regulationRepository;
    private final ChecklistItemRepository checklistItemRepository;

    // 근거 확보 — 없으면 생성해 저장(호출자 트랜잭션에서 dirty checking 으로 저장), 출처는 규제에서
    public TripMedicationBasisRes getOrGenerate(TripMedication tripMedication) {
        if (tripMedication.getBasisSummary() == null || tripMedication.getBasisSummary().isBlank()) {
            tripMedication.updateBasis(generate(tripMedication));
        }

        String countryCode = tripMedication.getTrip().getDestinationCountry().getCode();
        IngredientRegulation regulation =
                findRegulation(tripMedication.getMedication().getProduct().getIngredients(), countryCode);

        return new TripMedicationBasisRes(
                tripMedication.getId(),
                tripMedication.getBasisSummary(),
                regulation != null ? resolveSource(countryCode) : null,
                regulation != null ? regulation.getSourceUrl() : null,
                regulation != null ? regulation.getVerifiedDate() : null);
    }

    private String generate(TripMedication tripMedication) {
        MfdsProduct product = tripMedication.getMedication().getProduct();
        List<String> requirementLabels = checklistItemRepository.findByTripMedication_Id(tripMedication.getId()).stream()
                .map(checklistItem -> checklistItem.getRequirementTemplate().getLabel())
                .toList();

        String userPrompt = MedicationBasisPrompt.user(
                product.getIngredients(),
                tripMedication.getCategoryName(),
                tripMedication.getQuantityCondition(),
                tripMedication.getTrip().getDestinationCountry().getNameKo(),
                tripMedication.isRegulated(),
                requirementLabels);
        return openAiClient.chat(MedicationBasisPrompt.SYSTEM, userPrompt);
    }

    // 성분들 중 규제에 걸린 첫 성분의 규제 정보 (출처·확인일)
    private IngredientRegulation findRegulation(List<String> ingredients, String countryCode) {
        if (ingredients == null) {
            return null;
        }
        return ingredients.stream()
                .map(ingredient -> regulationRepository.findByCountry_CodeAndSubstanceName(countryCode, ingredient))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .findFirst()
                .orElse(null);
    }

    // 국가별 규제 출처 이름 (현재 일본만 지원)
    private String resolveSource(String countryCode) {
        return "JP".equals(countryCode) ? "일본 후생노동성" : "현지 보건당국";
    }
}
