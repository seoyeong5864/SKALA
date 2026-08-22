package com.skala.helpdesk.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository {

    private final Map<Integer, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();

    public Ticket request(String orderId, String type, String reason, String userId) {
        int no = sequence.incrementAndGet();
        Ticket t = new Ticket(no, orderId, type, reason, userId, Status.PENDING);
        tickets.put(no, t);
        return t;
    }

    public List<Ticket> pending() {
        return tickets.values().stream()
                .filter(t -> t.status() == Status.PENDING)
                .toList();
    }

    public Optional<Ticket> approve(int no) {
        return Optional.ofNullable(tickets.computeIfPresent(no,
                (id, t) -> new Ticket(t.no(), t.orderId(), t.type(), t.reason(), t.userId(), Status.APPROVED)));
    }

    public enum Status { PENDING, APPROVED }

    public record Ticket(int no, String orderId, String type, String reason, String userId, Status status) {}
}
