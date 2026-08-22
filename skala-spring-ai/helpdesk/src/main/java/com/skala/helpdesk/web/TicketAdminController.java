package com.skala.helpdesk.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skala.helpdesk.repository.TicketRepository;
import com.skala.helpdesk.repository.TicketRepository.Ticket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/tickets")
@Tag(name = "HelpDesk 관리 / 티켓 승인")
public class TicketAdminController {

    private final TicketRepository tickets;

    public TicketAdminController(TicketRepository tickets) {
        this.tickets = tickets;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "대기 중인 교환·환불 티켓 목록", description = "ADMIN 인증 필요.")
    public List<Ticket> pending() {
        return tickets.pending();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{no}/approve")
    @Operation(summary = "교환·환불 티켓 승인 처리", description = "모델은 호출할 수 없는 담당자 전용 API. ADMIN 인증 필요.")
    public Ticket approve(@PathVariable int no) {
        return tickets.approve(no).orElseThrow(() -> new TicketNotFoundException(no));
    }
}
