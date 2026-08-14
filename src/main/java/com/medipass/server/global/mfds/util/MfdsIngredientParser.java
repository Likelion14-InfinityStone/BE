package com.medipass.server.global.mfds.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MFDS 품목 필드 파싱 — 성분명(MAIN_INGR_ENG)과 정당 함량(ITEM_ENG_NAME)
 */
public final class MfdsIngredientParser {

    // 판정 매칭을 방해하는 염 접미어 (INN 매칭 전 제거)
    private static final Set<String> SALTS = Set.of(
            "hydrochloride", "hcl", "sulfate", "hydrobromide", "phosphate", "maleate",
            "mesylate", "citrate", "besylate", "tartrate", "bitartrate", "fumarate",
            "acetate", "succinate", "nitrate", "sodium", "potassium");

    // 영문 제품명에 붙은 함량 토큰 (예: 27mg, 0.5mg, 1g) — mg/g 만 인정
    private static final Pattern CONTENT = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(mg|g)\\b", Pattern.CASE_INSENSITIVE);

    private MfdsIngredientParser() {
    }

    // MAIN_INGR_ENG 를 |·/·; 로 분리하고 염을 뗀 성분명 목록 (INN 비교용, 중복 제거)
    public static List<String> parseEnglishNames(String english) {
        if (english == null || english.isBlank()) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (String part : english.split("[|/;]")) {
            String name = stripSalt(part.strip());
            if (!name.isBlank() && !names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    // ITEM_ENG_NAME 에서 정당 함량(mg)을 추출 — 그램은 ×1000, 함량 없거나 mg/g 아니면 null
    public static BigDecimal parseContentMg(String itemEngName) {
        if (itemEngName == null || itemEngName.isBlank()) {
            return null;
        }
        Matcher matcher = CONTENT.matcher(itemEngName);
        if (!matcher.find()) {
            return null;
        }
        BigDecimal amount = new BigDecimal(matcher.group(1));
        return matcher.group(2).equalsIgnoreCase("g") ? amount.multiply(BigDecimal.valueOf(1000)) : amount;
    }

    // 마지막 단어가 염이면 제거
    private static String stripSalt(String name) {
        String[] words = name.split("\\s+");
        if (words.length >= 2 && SALTS.contains(words[words.length - 1].toLowerCase())) {
            return String.join(" ", Arrays.copyOf(words, words.length - 1));
        }
        return name;
    }
}
