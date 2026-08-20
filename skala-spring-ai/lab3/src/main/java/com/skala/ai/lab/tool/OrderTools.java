package com.skala.ai.lab.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.ai.lab.dto.OrderResponse;
import com.skala.ai.lab.repository.OrderRepository;
import com.skala.ai.lab.service.OrderNotFoundException;

// 도구 호출 로그·감사 지표는 ToolAuditAspect가 한 곳에서 담당한다 — 여기서 따로 찍지 않는다.
@Component
public class OrderTools {

    private final OrderRepository orders;

    public OrderTools(OrderRepository orders) {
        this.orders = orders;
    }

    @Tool(description = """
            주문 상태를 조회한다. 사용자가 주문번호를 말하거나
            '내 주문', '배송 언제 와요' 처럼 물으면 이 도구를 쓴다.
            """) // ← 모델이 보는 것은 이 문장뿐이다
    public OrderResponse getOrder(
            @ToolParam(description = "조회할 주문번호. 예: 12345") String orderId,
            ToolContext context) { // ← 사용자 ID 는 모델이 아니라 여기서

        String userId = (String) context.getContext().get("userId");

        return orders.findByIdAndOwnerId(orderId, userId) // 권한은 쿼리 안에 넣는다
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
