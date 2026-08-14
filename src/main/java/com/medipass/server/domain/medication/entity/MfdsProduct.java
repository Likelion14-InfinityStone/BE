package com.medipass.server.domain.medication.entity;

import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 식약처 제품 마스터 — 품목코드(ITEM_SEQ)당 1행, 여러 사용자의 복약카드가 공유 참조.
 * 제품이 처음 등록될 때 MFDS를 1회 조회해 채우고, 이후에는 이 테이블만 읽는다 (재호출 없음).
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mfds_product")
public class MfdsProduct extends BaseEntity {

    // 식약처 품목기준코드(ITEM_SEQ) — 제품 식별자이자 PK
    @Id
    @Column(name = "mfds_product_code", length = 30)
    private String mfdsProductCode;

    // 한글 제품명 (ITEM_NAME)
    @Column(name = "product_ko_name", nullable = false, length = 300)
    private String productKoName;

    // 영문 제품명 (ITEM_ENG_NAME) — 함량(contentMg)을 여기서 파싱하므로 필수
    @Column(name = "product_en_name", nullable = false, length = 300)
    private String productEnName;

    // 영문 성분명 목록 (MAIN_INGR_ENG 파싱·염 제거) — 복합제면 여러 개
    @ElementCollection
    @CollectionTable(
            name = "mfds_product_ingredient",
            joinColumns = @JoinColumn(name = "mfds_product_code")
    )
    @Column(name = "ingredient", length = 200)
    @Builder.Default
    private List<String> ingredients = new ArrayList<>();

    // 정당 함량(mg) — ITEM_ENG_NAME 파싱, 없으면 null
    @Column(name = "content_mg", precision = 12, scale = 4)
    private BigDecimal contentMg;

    // 제조/수입사 (ENTP_NAME), 없으면 null
    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    // MFDS 조회 결과로 제품 마스터 생성 (등록 최초 1회)
    public static MfdsProduct of(String mfdsProductCode, String productKoName, String productEnName,
                                 List<String> ingredients, BigDecimal contentMg, String manufacturer) {
        return MfdsProduct.builder()
                .mfdsProductCode(mfdsProductCode)
                .productKoName(productKoName)
                .productEnName(productEnName)
                .ingredients(ingredients == null ? new ArrayList<>() : ingredients)
                .contentMg(contentMg)
                .manufacturer(manufacturer)
                .build();
    }
}
