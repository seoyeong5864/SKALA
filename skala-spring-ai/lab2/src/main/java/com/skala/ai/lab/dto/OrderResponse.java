package com.skala.ai.lab.dto;

import java.time.LocalDate;

import com.skala.ai.lab.domain.Order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrderResponse", description = "주문 응답 — 내부 필드(ownerId·cost)는 담기지 않는다")
public record OrderResponse(
        @Schema(description = "주문번호", example = "12345") String orderId,
        @Schema(description = "상품명", example = "무선 이어폰") String item,
        @Schema(description = "상태(화면 문구)", example = "배송중") String status,
        @Schema(description = "주문일", example = "2026-07-26") LocalDate orderedAt,
        @Schema(description = "도착 예정일", example = "2026-07-30") LocalDate eta) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getItem(),
                order.getStatus().label(), order.getOrderedAt(), order.getEta());
    }
}
