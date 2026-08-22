package com.skala.helpdesk.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.helpdesk.repository.OrderRepository;

@Component
public class OrderTools {

    private final OrderRepository orders;
    private final ToolCallLimiter limiter;

    public OrderTools(OrderRepository orders, ToolCallLimiter limiter) {
        this.orders = orders;
        this.limiter = limiter;
    }

    @Tool(description = "주문번호로 배송 상태와 예상 도착일을 조회한다. 사용자가 주문번호를 말하거나 "
            + "'내 주문', '배송 언제 와요'처럼 물으면 이 도구를 사용한다.")
    String orderStatus(@ToolParam(description = "조회할 주문번호. 예: 12345") String orderId, ToolContext ctx) {
        String conversationId = (String) ctx.getContext().get(ToolCallLimiter.CONTEXT_KEY);
        if (conversationId != null && !limiter.tryAcquire(conversationId)) {
            return "이 대화에서 도구 호출 한도를 초과했습니다. 새 대화로 다시 시도해 주세요.";
        }

        ToolInvocationTracker.markInvoked("orderStatus");
        String userId = (String) ctx.getContext().get("userId");
        return orders.findOwned(orderId, userId) // 소유자 검증 필수
                .map(o -> "주문 %s · 상태 %s · 예상도착 %s".formatted(o.getId(), o.getStatus().label(), o.getEta()))
                .orElse("해당 주문을 찾을 수 없습니다.");
    }
}
