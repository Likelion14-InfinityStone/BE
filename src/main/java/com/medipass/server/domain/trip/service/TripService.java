package com.medipass.server.domain.trip.service;

import com.medipass.server.domain.country.entity.Country;
import com.medipass.server.domain.country.repository.CountryRepository;
import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.repository.MedicationRepository;
import com.medipass.server.domain.medication.web.dto.MedicationListRes;
import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.regulation.repository.RequirementTemplateRepository;
import com.medipass.server.domain.regulation.service.RegulationJudgeService;
import com.medipass.server.domain.regulation.web.dto.JudgeReq;
import com.medipass.server.domain.regulation.web.dto.JudgeRes;
import com.medipass.server.domain.trip.entity.ChecklistItem;
import com.medipass.server.domain.trip.entity.Trip;
import com.medipass.server.domain.trip.entity.TripMedication;
import com.medipass.server.domain.trip.exception.ChecklistItemNotFoundException;
import com.medipass.server.domain.trip.exception.ChecklistUploadNotToggleableException;
import com.medipass.server.domain.trip.exception.TripAccessDeniedException;
import com.medipass.server.domain.trip.exception.TripMedicationNotFoundException;
import com.medipass.server.domain.trip.exception.TripNotFoundException;
import com.medipass.server.domain.trip.exception.TripTitleDuplicateException;
import com.medipass.server.domain.trip.repository.ChecklistItemRepository;
import com.medipass.server.domain.trip.repository.TripMedicationRepository;
import com.medipass.server.domain.trip.repository.TripRepository;
import com.medipass.server.domain.trip.util.DosageCalculator;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeReq;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeRes;
import com.medipass.server.domain.trip.web.dto.TripChecklogRes;
import com.medipass.server.domain.trip.web.dto.TripCreateReq;
import com.medipass.server.domain.trip.web.dto.TripCreateRes;
import com.medipass.server.domain.trip.web.dto.TripDetailRes;
import com.medipass.server.domain.trip.web.dto.TripMedicationBasisRes;
import com.medipass.server.domain.trip.web.dto.TripMedicationChecklistRes;
import com.medipass.server.domain.trip.web.dto.TripMedicationDestinationRes;
import com.medipass.server.domain.trip.web.dto.TripTitleUpdateRes;
import com.medipass.server.domain.user.entity.User;
import com.medipass.server.domain.user.repository.UserRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripMedicationRepository tripMedicationRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final CountryRepository countryRepository;
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final RequirementTemplateRepository templateRepository;
    private final RegulationJudgeService judgeService;
    private final MedicationJudgmentAssembler judgmentAssembler;
    private final MedicationBasisService medicationBasisService;

    // ─────────────────────────── 약 선택 (여행 등록 중) ───────────────────────────

    // 여행 등록 중 가져갈 약 선택용 — 내 복약카드 목록 (최근 등록순)
    @Transactional(readOnly = true)
    public MedicationListRes selectableMedications(Long userId) {
        return MedicationListRes.from(medicationRepository.findByUser_IdOrderByCreatedAtDesc(userId));
    }

    // ─────────────────────────── 분석 (판정, 저장 X) ───────────────────────────

    @Transactional(readOnly = true)
    public TripAnalyzeRes analyze(Long userId, TripAnalyzeReq request) {
        Country destination = findCountry(request.destinationCodeAlpha3());
        List<TripAnalyzeRes.MedicationResult> results = request.medications().stream()
                .map(item -> analyzeOne(userId, destination.getCode(), item))
                .toList();
        return new TripAnalyzeRes(results);
    }

    private TripAnalyzeRes.MedicationResult analyzeOne(Long userId, String countryCode, TripAnalyzeReq.Item item) {
        Medication medication = loadOwnedMedication(userId, item.medicationId());

        List<JudgeReq.Ingredient> ingredients = resolveIngredients(medication, item.carryDays());
        JudgeRes judged = judgeService.judge(new JudgeReq(countryCode, ingredients));

        return judgmentAssembler.assemble(medication, countryCode, item.carryDays(), judged);
    }

    // ─────────────────────────── 저장 (analyze 결과를 그대로 받아 저장) ───────────────────────────

    @Transactional
    public TripCreateRes createTrip(Long userId, TripCreateReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorResponseCode.NOT_FOUND_RESOURCE, "사용자를 찾을 수 없습니다."));

        Country origin = findCountry(request.origin().codeAlpha3());
        Country destination = findCountry(request.destination().codeAlpha3());
        String title = resolveTitle(userId, request.title(), destination);

        Trip trip = tripRepository.save(Trip.of(
                user, title,
                origin, request.origin().airportCode(), request.origin().city(),
                destination, request.destination().airportCode(), request.destination().city(),
                request.departOn(), request.returnOn()));

        List<TripCreateRes.MedicationResult> results = new ArrayList<>();
        for (TripCreateReq.MedicationItem item : request.medications()) {
            results.add(saveTripMedication(trip, userId, item));
        }
        return new TripCreateRes(trip.getId(), title, results);
    }

    private TripCreateRes.MedicationResult saveTripMedication(Trip trip, Long userId, TripCreateReq.MedicationItem item) {
        Medication medication = loadOwnedMedication(userId, item.medicationId());

        // analyze 판정 스냅샷 그대로 저장 (신호등·통제여부·분류·수량조건)
        TripMedication tripMedication = tripMedicationRepository.save(
                TripMedication.of(trip, medication, item.carryDays(), item.preparationLevel(),
                        item.regulated(), item.categoryCode(), item.categoryName(), item.quantityCondition()));

        // analyze 가 준 서류 templateId 로 체크리스트 생성
        if (item.requirementTemplateIds() != null) {
            for (Long templateId : item.requirementTemplateIds()) {
                templateRepository.findById(templateId).ifPresent(template ->
                        checklistItemRepository.save(ChecklistItem.of(tripMedication, template)));
            }
        }

        return new TripCreateRes.MedicationResult(
                tripMedication.getId(), medication.getId(),
                medication.getProduct().getProductKoName(), item.preparationLevel());
    }

    // ─────────────────────────── 체크로그함 (조회) ───────────────────────────

    // 상단 국가 칩(최신 등록국이 앞) + 선택 국가(기본=최신)의 여행(출국일 빠른 순)
    @Transactional(readOnly = true)
    public TripChecklogRes checklog(Long userId, String countryCode) {
        List<Trip> trips = tripRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        LocalDate today = LocalDate.now();

        // createdAt desc 순회 → 도착국 첫 등장 순서가 곧 최신순 (칩 목록)
        Map<String, Country> chipByCode = new LinkedHashMap<>();
        for (Trip trip : trips) {
            chipByCode.putIfAbsent(trip.getDestinationCountry().getCode(), trip.getDestinationCountry());
        }
        List<TripChecklogRes.CountryChip> countries = chipByCode.values().stream()
                .map(TripChecklogRes.CountryChip::from)
                .toList();

        // 선택 국가 — 요청값이 있으면 그 국가, 없으면 최신(첫 칩)
        String selected = (countryCode != null && !countryCode.isBlank())
                ? countryCode.toUpperCase()
                : (countries.isEmpty() ? null : countries.get(0).countryCode());

        // 다가오는 여행(dday>=0) 먼저 가까운 순, 지난 여행(dday<0)은 아래로 최근 지난 순
        List<TripChecklogRes.TripItem> items = trips.stream()
                .filter(trip -> trip.getDestinationCountry().getCode().equals(selected))
                .map(trip -> TripChecklogRes.TripItem.from(trip, today))
                .sorted(Comparator
                        .comparing((TripChecklogRes.TripItem t) -> t.dday() < 0)
                        .thenComparingLong(t -> Math.abs(t.dday())))
                .toList();

        return new TripChecklogRes(countries, selected, items);
    }

    // ─────────────────────────── 상세 (여행 티켓) ───────────────────────────

    // 여행 상세 — 여행 카드 + 담긴 약(신호등) 목록
    @Transactional(readOnly = true)
    public TripDetailRes getTrip(Long userId, Long tripId) {
        Trip trip = loadOwnedTrip(userId, tripId);
        List<TripMedication> medications = tripMedicationRepository.findByTrip_Id(tripId);
        return TripDetailRes.from(trip, medications, LocalDate.now());
    }

    // 약 상세 - 목적지 규정 — 헤더(제품·성분·함량) + 판정 스냅샷 + 필요 서류
    @Transactional(readOnly = true)
    public TripMedicationDestinationRes getDestinationRules(Long userId, Long tripId, Long tripMedicationId) {
        loadOwnedTrip(userId, tripId); // 404/403 가드

        TripMedication tripMedication = tripMedicationRepository.findById(tripMedicationId)
                .filter(tm -> tm.getTrip().getId().equals(tripId))
                .orElseThrow(TripMedicationNotFoundException::new);

        List<ChecklistItem> checklist = checklistItemRepository.findByTripMedication_Id(tripMedicationId);
        return TripMedicationDestinationRes.from(tripMedication, checklist);
    }

    // 약 상세 - 근거 — 저장된 AI 근거 반환, 없으면 생성해 저장 후 반환
    @Transactional
    public TripMedicationBasisRes getBasis(Long userId, Long tripId, Long tripMedicationId) {
        loadOwnedTrip(userId, tripId); // 404/403 가드

        TripMedication tripMedication = tripMedicationRepository.findById(tripMedicationId)
                .filter(tm -> tm.getTrip().getId().equals(tripId))
                .orElseThrow(TripMedicationNotFoundException::new);

        return medicationBasisService.getOrGenerate(tripMedication);
    }

    // 약 상세 - 체크리스트 조회 (진행률 + 서류 항목)
    @Transactional(readOnly = true)
    public TripMedicationChecklistRes getChecklist(Long userId, Long tripId, Long tripMedicationId) {
        loadOwnedTrip(userId, tripId); // 404/403 가드
        tripMedicationRepository.findById(tripMedicationId)
                .filter(tm -> tm.getTrip().getId().equals(tripId))
                .orElseThrow(TripMedicationNotFoundException::new);

        return TripMedicationChecklistRes.from(
                tripMedicationId, checklistItemRepository.findByTripMedication_Id(tripMedicationId));
    }

    // 약 상세 - 체크리스트 항목 체크/해제
    @Transactional
    public TripMedicationChecklistRes updateChecklistDone(
            Long userId, Long tripId, Long tripMedicationId, Long checklistItemId, boolean done) {
        loadOwnedTrip(userId, tripId); // 404/403 가드

        ChecklistItem item = checklistItemRepository.findById(checklistItemId)
                .filter(ci -> ci.getTripMedication().getId().equals(tripMedicationId)
                        && ci.getTripMedication().getTrip().getId().equals(tripId))
                .orElseThrow(ChecklistItemNotFoundException::new);

        // 업로드 서류는 파일 업로드로만 완료됨 — 토글 대상은 신청·문의(ACTION)뿐
        if (item.getRequirementTemplate().getKind() == RequirementKind.UPLOAD) {
            throw new ChecklistUploadNotToggleableException();
        }
        item.updateDone(done);

        return TripMedicationChecklistRes.from(
                tripMedicationId, checklistItemRepository.findByTripMedication_Id(tripMedicationId));
    }

    // ─────────────────────────── 이름 수정 ───────────────────────────

    // 여행 제목 변경 — 같은 사용자 안에서 이름이 겹치면 409
    @Transactional
    public TripTitleUpdateRes updateTitle(Long userId, Long tripId, String title) {
        Trip trip = loadOwnedTrip(userId, tripId);
        String newTitle = title.trim();

        if (tripRepository.existsByUser_IdAndTitleAndIdNot(userId, newTitle, tripId)) {
            throw new TripTitleDuplicateException();
        }

        trip.updateTitle(newTitle);
        return TripTitleUpdateRes.from(trip);
    }

    // ─────────────────────────── 공통 ───────────────────────────

    private Trip loadOwnedTrip(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(TripNotFoundException::new);
        if (!trip.getUser().getId().equals(userId)) {
            throw new TripAccessDeniedException();
        }
        return trip;
    }

    private Medication loadOwnedMedication(Long userId, Long medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new BaseException(ErrorResponseCode.NOT_FOUND_RESOURCE, "약을 찾을 수 없습니다."));
        if (!medication.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_RESOURCE, "약을 찾을 수 없습니다.");
        }
        return medication;
    }

    private Country findCountry(String alpha3) {
        return countryRepository.findByCodeAlpha3(alpha3.toUpperCase())
                .orElseThrow(() -> new BaseException(ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "지원하지 않는 국가입니다: " + alpha3));
    }

    // 제목 없으면 "{도착국} 여행N" — 겹치지 않는 다음 번호
    private String resolveTitle(Long userId, String requested, Country destination) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        String base = destination.getNameKo() + " 여행";
        int n = 1;
        String title;
        do {
            title = base + n++;
        } while (tripRepository.existsByUser_IdAndTitle(userId, title));
        return title;
    }

    // ─────────────────────────── 성분·총량 (제품 마스터) ───────────────────────────

    /**
     * 약의 성분 + 총 반입량(mg)을 판정용으로 변환 — 제품 마스터(성분·함량)에서 읽음 (MFDS 재호출 X)
     * 단일 성분이고 함량이 있으면 총량을 계산해 넘기고, 그 외는 이름만(양 null → 보수적 판정)
     */
    private List<JudgeReq.Ingredient> resolveIngredients(Medication med, int carryDays) {
        List<String> names = med.getProduct().getIngredients();
        if (names.isEmpty()) {
            return List.of();
        }

        BigDecimal mgPerUnit = med.getProduct().getContentMg();
        BigDecimal totalUnits = DosageCalculator.totalUnits(med.getIntakesPerDay(), med.getDosePerIntake(), carryDays);

        if (names.size() == 1 && mgPerUnit != null && totalUnits != null) {
            BigDecimal totalMg = mgPerUnit.multiply(totalUnits);
            return List.of(new JudgeReq.Ingredient(names.get(0), totalMg));
        }
        return names.stream().map(n -> new JudgeReq.Ingredient(n, null)).toList();
    }
}
