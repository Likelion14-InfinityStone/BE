package com.medipass.server.domain.regulation.web.dto;

import com.medipass.server.domain.regulation.entity.RequirementKind;

/**
 * 나라별 Source가 내놓는 서류 템플릿 1건, 이 형태 그대로 requirement_template 에 upsert
 */
public record RequirementTemplateRecord(
        String categoryCode,
        RequirementKind kind,
        String label,
        String formUrl,
        String description
) {
}
