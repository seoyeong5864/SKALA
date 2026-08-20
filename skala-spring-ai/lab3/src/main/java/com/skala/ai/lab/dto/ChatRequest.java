package com.skala.ai.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChatRequest", description = "상담 에이전트 요청 — userId는 실습 편의상 파라미터로 받는다(실서비스라면 인증에서 꺼낸다)")
public record ChatRequest(
        @Schema(description = "질문", example = "12345 어디쯤이야?") String question,
        @Schema(description = "요청자 ID", example = "user1") String userId,
        @Schema(description = "대화 세션 ID(멀티턴 유지용, 없으면 매번 새 대화)", example = "") String sessionId) {
}
