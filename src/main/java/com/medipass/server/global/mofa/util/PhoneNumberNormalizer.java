package com.medipass.server.global.mofa.util;

import java.util.regex.Pattern;

/**
 * 전화번호 정규화 — 외교부가 주는 제각각인 표기를 E.164 (+국가번호+번호) 로 통일
 *
 * 같은 응답 안에서 표기가 네 가지로 섞여 온다.
 *   +81-3-3452-7611 / (81) 92-771-0461 / (82)2-3210-0404 / (82) 2-3210-0404
 * 뒤 두 개는 같은 번호인데 공백 하나 때문에 문자열이 달라서, 그대로 두면 비교가 안 되고
 * 앱에서 tel: 링크도 걸리지 않는다.
 */
public final class PhoneNumberNormalizer {

    /*
     * 국가번호는 원문에 '+81' 또는 '(81)' 처럼 표시된 경우만 인정한다.
     * 숫자만 보고 판단하면 '(81) 82-505-2100' 의 82(히로시마 지역번호)를 국가번호로 오인한다.
     */
    private static final Pattern COUNTRY_CODE_MARK = Pattern.compile("^\\s*[+(]\\s*\\d{1,3}\\s*\\)?");
    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private static final String KOREA_CODE = "82";
    // 영사콜센터(서울) 02-3210-0404 — 현지 번호 자리에 이게 오는 공관이 있다
    private static final String CONSULAR_CENTER_DIGITS = "32100404";

    private PhoneNumberNormalizer() {
    }

    /**
     * E.164 로 변환. 국가번호 표시가 없으면 국제전화로 걸 수 없는 번호이므로 null 을 반환한다.
     * (걸리지 않는 번호를 화면에 띄우느니 그 줄을 비우는 편이 낫다)
     */
    public static String toE164(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = digitsOf(raw);
        if (digits.isEmpty() || !COUNTRY_CODE_MARK.matcher(raw).find()) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * 한국 번호인지 — 재외공관 긴급연락처 자리에 한국 영사콜센터(+82-2-3210-0404)가
     * 들어오는 경우가 있어서 판별이 필요하다. 현지 번호로 알고 걸면 국제전화 요금이 나간다.
     */
    public static boolean isKoreanNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String digits = digitsOf(raw);
        return digits.startsWith(KOREA_CODE) || digits.contains(CONSULAR_CENTER_DIGITS);
    }

    private static String digitsOf(String raw) {
        return NON_DIGIT.matcher(raw).replaceAll("");
    }
}
