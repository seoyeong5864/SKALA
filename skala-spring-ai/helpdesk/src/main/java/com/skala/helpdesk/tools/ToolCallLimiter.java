package com.skala.helpdesk.tools;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.skala.helpdesk.config.HelpDeskProperties;

// 대화당 도구 호출 횟수 상한
@Component
public class ToolCallLimiter {

    public static final String CONTEXT_KEY = "conversationId";

    private final int maxCallsPerConversation;
    private final ConcurrentHashMap<String, AtomicInteger> callsByConversation = new ConcurrentHashMap<>();

    public ToolCallLimiter(HelpDeskProperties props) {
        this.maxCallsPerConversation = props.chat().maxToolCallsPerConversation();
    }

    public boolean tryAcquire(String conversationId) {
        int count = callsByConversation
                .computeIfAbsent(conversationId, id -> new AtomicInteger(0))
                .incrementAndGet();
        return count <= maxCallsPerConversation;
    }
}
