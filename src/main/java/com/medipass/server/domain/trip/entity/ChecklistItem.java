package com.medipass.server.domain.trip.entity;

import com.medipass.server.domain.regulation.entity.RequirementTemplate;
import jakarta.persistence.*;
import lombok.*;

/**
 * 체크리스트 항목 (여행별 준비 진행상황)
 * 여행에 챙긴 약(trip_medication) 하나에 필요한 서류 하나를 대응시키고, 완료 여부를 관리한다
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "checklist_item")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 여행-약에 대한 준비 항목인가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_medication_id", nullable = false)
    private TripMedication tripMedication;

    // 어떤 서류인가 (분류별 서류 템플릿)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requirement_template_id", nullable = false)
    private RequirementTemplate requirementTemplate;

    // 완료 여부
    @Column(name = "done", nullable = false)
    private boolean done;

    // TODO: 서류 업로드(S3, document) 연결은 서류함 범위에서 추가

    // 여행-약에 필요한 서류 한 건 — 미완료 상태로 생성
    public static ChecklistItem of(TripMedication tripMedication, RequirementTemplate requirementTemplate) {
        return ChecklistItem.builder()
                .tripMedication(tripMedication)
                .requirementTemplate(requirementTemplate)
                .done(false)
                .build();
    }

    // 체크(완료) 여부 변경
    public void updateDone(boolean done) {
        this.done = done;
    }
}
