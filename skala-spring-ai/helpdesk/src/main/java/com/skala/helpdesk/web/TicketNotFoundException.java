package com.skala.helpdesk.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(int no) {
        super("티켓을 찾을 수 없습니다: " + no);
    }
}
