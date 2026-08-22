package com.skala.helpdesk.advisor;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.skala.helpdesk.tools.ToolInvocationTracker;

@Component
public class AuditAdvisor implements BaseAdvisor {

    private static final Logger audit = LoggerFactory.getLogger("AI_AUDIT");
    private static final String START_NS = "auditStartNs";
    public static final String TRACE_ID = "traceId";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // ToolContext는 .toolContext(Map)으로 넣은 값에서 온다 — userId가 여기 담겨 온다
        Map<String, Object> toolContext = request.prompt().getOptions() instanceof ToolCallingChatOptions options
                ? options.getToolContext() : Map.of();

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        Object userId = toolContext.get("userId");
        String question = request.prompt().getUserMessage() == null
                ? "" : request.prompt().getUserMessage().getText();

        audit.info("[{}] 요청 userId={} question=\"{}\"", traceId, userId, PiiMasker.mask(question));

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

        ChatResponse chatResponse = response.chatResponse();
        boolean success = chatResponse != null && chatResponse.getResult() != null
                && chatResponse.getResult().getOutput() != null;

        audit.info("[{}] 응답 {}ms · tools={} · success={}",
                traceId, elapsedMs, ToolInvocationTracker.invoked(), success);
        return response;
    }
}
