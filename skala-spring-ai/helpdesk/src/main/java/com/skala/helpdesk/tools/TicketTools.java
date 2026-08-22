package com.skala.helpdesk.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.helpdesk.repository.OrderRepository;
import com.skala.helpdesk.repository.TicketRepository;

@Component
public class TicketTools {

    private final OrderRepository orders;
    private final TicketRepository tickets;
    private final ToolCallLimiter limiter;

    public TicketTools(OrderRepository orders, TicketRepository tickets, ToolCallLimiter limiter) {
        this.orders = orders;
        this.tickets = tickets;
        this.limiter = limiter;
    }

    @Tool(description = "본인 주문의 교환·환불 티켓을 접수한다. 접수만 할 뿐 즉시 처리되지 않으며, "
            + "담당자 승인 후 진행된다. 사용자가 '교환해주세요', '환불하고 싶어요'처럼 요청하면 이 도구를 사용한다.")
    String createTicket(@ToolParam(description = "교환·환불 대상 주문번호. 예: 12345") String orderId,
                         @ToolParam(description = "EXCHANGE 또는 REFUND 중 하나") String type,
                         @ToolParam(description = "교환·환불 사유. 예: 사이즈가 안 맞음") String reason,
                         ToolContext ctx) {
        String conversationId = (String) ctx.getContext().get(ToolCallLimiter.CONTEXT_KEY);
        if (conversationId != null && !limiter.tryAcquire(conversationId)) {
            return "이 대화에서 도구 호출 한도를 초과했습니다. 새 대화로 다시 시도해 주세요.";
        }

        ToolInvocationTracker.markInvoked("createTicket");
        String userId = userOf(ctx);
        if (orders.findOwned(orderId, userId).isEmpty()) { // 소유자 검증 필수
            return "해당 주문을 찾을 수 없습니다.";
        }
        var t = tickets.request(orderId, type, reason, userId);
        return "티켓 %s 를 접수했습니다. 승인 후 처리됩니다.".formatted(t.no());
    }

    private String userOf(ToolContext ctx) {
        return (String) ctx.getContext().get("userId");
    }
}
