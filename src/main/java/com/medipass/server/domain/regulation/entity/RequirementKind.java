package com.medipass.server.domain.regulation.entity;

/**
 * 준비항목 성격
 */
public enum RequirementKind {
    UPLOAD, // 사용자가 서류 업로드 (진단서·약 사진 등)
    ACTION  // 외부 절차 (신청서 작성·제출)
}
