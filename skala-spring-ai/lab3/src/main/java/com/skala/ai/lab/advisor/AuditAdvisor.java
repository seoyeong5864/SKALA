package com.skala.ai.lab.advisor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.core.Ordered;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 체인의 가장 바깥에서 이 턴에 관련된 모든 로그 줄 앞에 traceId를 붙인다.
 * SafetyAdvisor가 안쪽에서 차단하더라도 이 advisor는 여전히 실행되므로, 차단 시도 자체도 기록된다.
 *
 * <p>order는 {@code Ordered.HIGHEST_PRECEDENCE} 기준으로 잡는다 — Spring AI가 자동 등록하는
 * {@code ToolCallingAdvisor}(기본 order {@code HIGHEST_PRECEDENCE+300})보다 반드시 바깥(작은 값)에 있어야
 * 도구 호출 왕복마다 재실행되지 않고 사용자 턴 하나당 한 번만 실행된다.
 */
public class AuditAdvisor implements BaseAdvisor {

    private static final Logger audit = LoggerFactory.getLogger("AI_AUDIT");
    private static final String START_NS = "auditStartNs";
    public static final String TRACE_ID = "traceId";

    private final MeterRegistry registry;

    public AuditAdvisor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "AuditAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // request.context()는 .advisors(a -> a.param(...))로 넣은 값만 담는 별도의 맵이다 —
        // @Tool 메서드가 받는 ToolContext는 .toolContext(Map)으로 넣은 값(ToolCallingChatOptions)에서
        // 온다. 두 경로가 다르므로, 도구까지 같은 traceId를 전달하려면 여기서 만들지 않고
        // AgentService가 .toolContext(...)에 미리 심어 둔 값을 읽어야 한다.
        Map<String, Object> toolContext = request.prompt().getOptions() instanceof ToolCallingChatOptions options
                ? options.getToolContext() : Map.of();

        String traceId = String.valueOf(
                toolContext.getOrDefault(TRACE_ID, UUID.randomUUID().toString().substring(0, 8)));
        Object userId = toolContext.get("userId");
        String question = request.prompt().getUserMessage() == null
                ? "" : request.prompt().getUserMessage().getText();

        audit.info("[{}] {}   질문=\"{}\"", traceId, userId, PiiMasker.mask(question));

        // Memory·QuestionAnswer 등 나머지 advisor 간 통신은 이 request.context()로 이어간다 —
        // 이 경로는 advisor끼리는 문제없이 전달된다(도구로 갈 때만 별도 경로를 타는 것).
        return request.mutate()
                .context(TRACE_ID, traceId)
                .context(START_NS, System.nanoTime())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        Object traceId = response.context().get(TRACE_ID);
        Object startObj = response.context().get(START_NS);
        long elapsedMs = startObj instanceof Long start ? (System.nanoTime() - start) / 1_000_000 : -1;

        registry.timer("ai.latency", "phase", "chat").record(elapsedMs, TimeUnit.MILLISECONDS);

        ChatResponse chatResponse = response.chatResponse();
        String finishReason = chatResponse == null
                ? "N/A" : String.valueOf(chatResponse.getResult().getMetadata().getFinishReason());

        audit.info("[{}]   응답 {}ms · finishReason={}", traceId, elapsedMs, finishReason);
        return response;
    }
}
