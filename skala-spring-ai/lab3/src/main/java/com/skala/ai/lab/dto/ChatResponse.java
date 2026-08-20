package com.skala.ai.lab.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChatResponse", description = "상담 에이전트 응답")
public record ChatResponse(
        @Schema(description = "답변") String answer,
        @Schema(description = "근거 문서 출처(정책 질문이 아니면 비어 있다)") List<String> sources) {
}
