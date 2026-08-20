package com.skala.ai.lab.service;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String ticketNo) {
        super("티켓을 찾을 수 없습니다: " + ticketNo);
    }
}
