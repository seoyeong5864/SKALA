package com.skala.ai.lab.tool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * 대화(conversationId)당 도구 호출 횟수 상한.
 * "도구를 계속 불러라" 식으로 반복을 유도하는 공격을 프롬프트가 아니라 카운터로 막는다.
 *
 * <p>인메모리라 재시작하면 초기화된다. 상한값은 실습에서 실제로 걸어볼 수 있도록 낮게 잡은
 * 테스트용 숫자이지 운영값이 아니다.
 */
@Component
public class ToolCallLimiter {

    /** ToolContext에 conversationId를 실어 보낼 때 쓰는 키 — AgentService와 ToolAuditAspect가 공유한다. */
    public static final String CONTEXT_KEY = "conversationId";

    private static final int MAX_CALLS_PER_CONVERSATION = 5;

    private final ConcurrentHashMap<String, AtomicInteger> callsByConversation = new ConcurrentHashMap<>();

    /** true면 이번 호출은 허용, false면 이미 상한을 넘겨서 거부해야 한다. */
    public boolean tryAcquire(String conversationId) {
        int count = callsByConversation
                .computeIfAbsent(conversationId, id -> new AtomicInteger(0))
                .incrementAndGet();
        return count <= MAX_CALLS_PER_CONVERSATION;
    }
}
