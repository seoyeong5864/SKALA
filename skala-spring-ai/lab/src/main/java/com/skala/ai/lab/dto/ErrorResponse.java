package com.skala.ai.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "실패 응답 — 상세 원인은 로그에만 남기고 traceId로 추적한다")
public record ErrorResponse(
        @Schema(description = "사용자에게 보여줄 메시지", example = "요약을 만들지 못했습니다. 잠시 후 다시 시도해 주세요.") String message,
        @Schema(description = "로그 추적용 ID (원인이 명확한 경우 없음)", example = "3f2a9c1d") String traceId) {
}
