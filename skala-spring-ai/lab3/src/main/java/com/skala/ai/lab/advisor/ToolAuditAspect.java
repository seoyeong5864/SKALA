package com.skala.ai.lab.advisor;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import com.skala.ai.lab.tool.ToolCallLimitExceededException;
import com.skala.ai.lab.tool.ToolCallLimiter;
import com.skala.ai.lab.tool.ToolInvocationTracker;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@code @Tool}이 붙은 모든 메서드 호출을 한 곳에서 감사한다.
 * 도구마다 로깅 코드를 넣으면 반드시 빠뜨리는 곳이 생긴다 — 그래서 여기 하나로 모은다.
 * 대화당 도구 호출 상한(레드팀 6번: 반복 유도)도 실행 전에 여기서 먼저 검사한다.
 *
 * <p>주의: 인자에 개인정보가 들어올 수 있다. 여기서는 아주 단순한 마스킹만 적용한다.
 */
@Aspect
@Component
public class ToolAuditAspect {

    private static final Logger audit = LoggerFactory.getLogger("AI_AUDIT");

    private final MeterRegistry registry;
    private final ToolCallLimiter limiter;

    public ToolAuditAspect(MeterRegistry registry, ToolCallLimiter limiter) {
        this.registry = registry;
        this.limiter = limiter;
    }

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String tool = joinPoint.getSignature().getName();
        ToolContext context = extractToolContext(joinPoint.getArgs());
        String traceId = contextValue(context, AuditAdvisor.TRACE_ID);
        String conversationId = contextValue(context, ToolCallLimiter.CONTEXT_KEY);
        String args = PiiMasker.mask(Arrays.toString(joinPoint.getArgs()));
        long started = System.nanoTime();

        if (!"-".equals(conversationId) && !limiter.tryAcquire(conversationId)) {
            ToolCallLimitExceededException limitExceeded = new ToolCallLimitExceededException();
            record(tool, traceId, args, started, "blocked", limitExceeded);
            throw limitExceeded; // 실행하지 않고 여기서 끊는다 — Spring AI가 이 메시지를 도구 결과로 모델에게 돌려준다
        }

        ToolInvocationTracker.markInvoked();

        try {
            Object result = joinPoint.proceed();
            record(tool, traceId, args, started, "ok", null);
            return result;
        } catch (Throwable e) {
            record(tool, traceId, args, started, "fail", e);
            throw e;
        }
    }

    private void record(String tool, String traceId, String args, long startedNs, String result, Throwable error) {
        long elapsedMs = (System.nanoTime() - startedNs) / 1_000_000;

        registry.counter("ai.tool.calls", "tool", tool, "result", result).increment();
        registry.timer("ai.latency", "phase", "tool").record(elapsedMs, TimeUnit.MILLISECONDS);

        if (error == null) {
            audit.info("[{}]   도구 {}({}) {}ms", traceId, tool, args, elapsedMs);
        } else {
            audit.warn("[{}]   도구 {}({}) {}ms {}={}", traceId, tool, args, elapsedMs, result, error.toString());
        }
    }

    /** traceId·conversationId는 프롬프트가 아니라 ToolContext로 온다 — AgentService가 심어 둔 값을 그대로 읽는다. */
    private ToolContext extractToolContext(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ToolContext context) {
                return context;
            }
        }
        return null;
    }

    private String contextValue(ToolContext context, String key) {
        if (context == null) {
            return "-";
        }
        Object value = context.getContext().get(key);
        return value == null ? "-" : value.toString();
    }
}
