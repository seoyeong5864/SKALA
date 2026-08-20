package com.skala.ai.lab.domain;

import java.time.Instant;

public record Ticket(String no, String orderId, String userId, String reason, Status status, Instant requestedAt) {

    public enum Status {
        PENDING, APPROVED
    }

    public Ticket approved() {
        return new Ticket(no, orderId, userId, reason, Status.APPROVED, requestedAt);
    }
}
