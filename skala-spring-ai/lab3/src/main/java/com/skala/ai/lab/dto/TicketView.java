package com.skala.ai.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TicketView", description = "환불 접수 결과 — 실제 처리 여부는 담기지 않는다")
public record TicketView(
        @Schema(description = "티켓 번호", example = "RF-1001") String ticketNo,
        @Schema(description = "안내 메시지") String message) {

    public static TicketView accepted(String ticketNo) {
        return new TicketView(ticketNo, "접수되었습니다(%s). 담당자 승인 후 처리됩니다.".formatted(ticketNo));
    }
}
