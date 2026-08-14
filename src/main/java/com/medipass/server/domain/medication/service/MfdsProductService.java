package com.medipass.server.domain.medication.service;

import com.medipass.server.domain.medication.entity.MfdsProduct;
import com.medipass.server.domain.medication.exception.MedicationProductNotFoundException;
import com.medipass.server.domain.medication.repository.MfdsProductRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.mfds.client.MfdsClient;
import com.medipass.server.global.mfds.dto.MfdsProductItem;
import com.medipass.server.global.mfds.util.MfdsIngredientParser;
import com.medipass.server.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 식약처 제품 마스터 확보 — 있으면 재사용, 없으면 MFDS 1회 조회해 저장 (lazy upsert)
 * 성분·함량은 여기서만 파싱해 마스터에 넣고, 이후 조회는 DB만 읽는다
 */
@Service
@RequiredArgsConstructor
public class MfdsProductService {

    private final MfdsProductRepository mfdsProductRepository;
    private final MfdsClient mfdsClient;

    // 품목코드로 제품 마스터 확보 (검색은 제품명으로, itemSeq 일치 건을 채택)
    @Transactional
    public MfdsProduct getOrCreate(String mfdsProductCode, String productNameForSearch) {
        return mfdsProductRepository.findById(mfdsProductCode)
                .orElseGet(() -> fetchAndSave(mfdsProductCode, productNameForSearch));
    }

    private MfdsProduct fetchAndSave(String mfdsProductCode, String productNameForSearch) {
        MfdsProductItem item = mfdsClient.searchByProductName(productNameForSearch).items().stream()
                .filter(it -> mfdsProductCode.equals(it.itemSeq()))
                .findFirst()
                .orElseThrow(MedicationProductNotFoundException::new);

        if (item.itemEngName() == null || item.itemEngName().isBlank()) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_RESOURCE,
                    "제품 영문명이 없어 등록할 수 없습니다.");
        }

        List<String> ingredients = MfdsIngredientParser.parseEnglishNames(item.mainIngredientEnglish());
        BigDecimal contentMg = MfdsIngredientParser.parseContentMg(item.itemEngName());

        MfdsProduct product = MfdsProduct.of(
                item.itemSeq(), item.itemName(), item.itemEngName(),
                ingredients, contentMg, item.manufacturer());
        return mfdsProductRepository.save(product);
    }
}
