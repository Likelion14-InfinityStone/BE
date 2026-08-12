package com.medipass.server.domain.regulation.web.controller;

import com.medipass.server.domain.regulation.service.RegulationJudgeService;
import com.medipass.server.domain.regulation.web.dto.JudgeReq;
import com.medipass.server.domain.regulation.web.dto.JudgeRes;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regulations")
public class RegulationJudgeController {

    private final RegulationJudgeService regulationJudgeService;

    @Operation(
            summary = "성분 규제 판정",
            description = "도착 국가 + 성분 목록(소지량 선택)을 받아 성분별 신호등·한도·필요서류를 반환한다."
    )
    @PostMapping("/judge")
    public ApiResponse<JudgeRes> judge(@Valid @RequestBody JudgeReq request) {
        return ApiResponse.success(regulationJudgeService.judge(request));
    }
}
