package com.skala.ai.lab.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.ai.lab.domain.Ticket;
import com.skala.ai.lab.dto.TicketView;
import com.skala.ai.lab.repository.OrderRepository;
import com.skala.ai.lab.service.OrderNotFoundException;
import com.skala.ai.lab.service.TicketStore;

/**
 * 되돌리기 어려운 행동(환불)은 접수까지만 도구에게 준다.
 * 실행(승인)은 {@code TicketAdminController}의 사람 전용 API로만 가능하다 — 모델이 닿을 수 없는 경로다.
 * 도구 호출 로그·감사 지표는 ToolAuditAspect가 한 곳에서 담당한다.
 */
@Component
public class RefundTools {

    private final OrderRepository orders;
    private final TicketStore tickets;

    public RefundTools(OrderRepository orders, TicketStore tickets) {
        this.orders = orders;
        this.tickets = tickets;
    }

    @Tool(description = """
            환불을 접수한다. 즉시 처리되지 않고 담당자 승인 후 처리된다.
            사용자가 명시적으로 환불·반품을 요청했을 때만 부른다.
            """)
    public TicketView requestRefund(
            @ToolParam(description = "환불할 주문번호") String orderId,
            @ToolParam(description = "환불 사유(사용자가 말한 그대로)") String reason,
            ToolContext context) {

        String userId = (String) context.getContext().get("userId");
        orders.findByIdAndOwnerId(orderId, userId) // 권한 먼저 — 남의 주문은 접수도 안 된다
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Ticket ticket = tickets.create(orderId, userId, reason); // 상태: PENDING
        return TicketView.accepted(ticket.no());
    }
}
