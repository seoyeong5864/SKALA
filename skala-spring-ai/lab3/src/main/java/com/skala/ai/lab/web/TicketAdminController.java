package com.skala.ai.lab.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skala.ai.lab.domain.Ticket;
import com.skala.ai.lab.service.TicketNotFoundException;
import com.skala.ai.lab.service.TicketStore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lab3/admin/tickets")
@Tag(name = "DAY3 실습 / 환불 승인(관리자)")
public class TicketAdminController {

    private final TicketStore tickets;

    public TicketAdminController(TicketStore tickets) {
        this.tickets = tickets;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "대기 중인 환불 티켓 목록", description = "ADMIN 인증 필요.")
    public List<Ticket> pending() {
        return tickets.pending();
    }

    // 실제 처리는 사람이 누른다 — 모델은 이 경로를 도구로 갖고 있지 않다.
    @PostMapping("/{no}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "환불 승인 처리", description = "모델은 호출할 수 없는 담당자 전용 API. ADMIN 인증 필요.")
    public Ticket approve(@PathVariable String no) {
        return tickets.approve(no).orElseThrow(() -> new TicketNotFoundException(no));
    }
}
