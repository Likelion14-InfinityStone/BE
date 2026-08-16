package com.medipass.server.domain.trip.entity;

import com.medipass.server.domain.document.entity.Document;
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

    // 업로드된 서류 — 체크리스트 항목당 최대 한 건
    @OneToOne(mappedBy = "checklistItem", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Document document;

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

    // 서류 연결과 체크리스트 완료 처리를 함께 수행한다.
    public void attachDocument(Document document) {
        this.document = document;
        this.done = true;
    }

    // 서류 연결을 해제하면 체크리스트도 미완료 상태로 되돌린다.
    public void detachDocument() {
        this.document = null;
        this.done = false;
    }
}
