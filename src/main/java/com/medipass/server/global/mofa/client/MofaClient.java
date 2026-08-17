package com.medipass.server.global.mofa.client;

import com.medipass.server.global.mofa.dto.MofaMissionItem;

import java.util.List;

public interface MofaClient {

    /** 국가 ISO alpha-2 코드로 그 나라의 재외공관 전체 조회 (일본 = 대사관 1 + 총영사관 9) */
    List<MofaMissionItem> findMissionsByCountry(String countryCode);
}
