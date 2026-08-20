package com.skala.ai.lab.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.skala.ai.lab.domain.Ticket;
import com.skala.ai.lab.domain.Ticket.Status;

/**
 * 환불 승인 게이트용 인메모리 저장소.
 * 접수(PENDING)만 만들고, 실제 승인은 관리자 API에서만 상태를 바꾼다 — 도구는 이 클래스의 approve()를 호출할 경로가 없다.
 */
@Component
public class TicketStore {

    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public Ticket create(String orderId, String userId, String reason) {
        String no = "RF-" + sequence.incrementAndGet();
        Ticket ticket = new Ticket(no, orderId, userId, reason, Status.PENDING, Instant.now());
        tickets.put(no, ticket);
        return ticket;
    }

    public Optional<Ticket> find(String no) {
        return Optional.ofNullable(tickets.get(no));
    }

    public List<Ticket> pending() {
        return tickets.values().stream()
                .filter(t -> t.status() == Status.PENDING)
                .toList();
    }

    public Optional<Ticket> approve(String no) {
        return find(no).map(t -> {
            Ticket approved = t.approved();
            tickets.put(no, approved);
            return approved;
        });
    }
}
