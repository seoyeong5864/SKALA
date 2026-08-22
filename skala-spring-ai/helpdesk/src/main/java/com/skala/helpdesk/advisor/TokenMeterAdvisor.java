package com.skala.helpdesk.advisor;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

// 지표를 남겨 비용, 성능을 관찰
@Component
public class TokenMeterAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger("AI_AUDIT");
    private static final String START_NS = "modelStartNs";

    private final MeterRegistry registry;

    public TokenMeterAdvisor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public int getOrder() {
        return 900;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        return request.mutate().context(START_NS, System.nanoTime()).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        Object startObj = response.context().get(START_NS);
        long elapsedMs = startObj instanceof Long start ? (System.nanoTime() - start) / 1_000_000 : -1;
        registry.timer("ai.latency", "phase", "model").record(elapsedMs, TimeUnit.MILLISECONDS);

        if (response.chatResponse() != null && response.chatResponse().getMetadata() != null) {
            Usage usage = response.chatResponse().getMetadata().getUsage();
            if (usage != null) {
                registry.counter("ai.tokens", "type", "prompt").increment(usage.getPromptTokens());
                registry.counter("ai.tokens", "type", "completion").increment(usage.getCompletionTokens());

                Object traceId = response.context().get(AuditAdvisor.TRACE_ID);
                log.info("[{}]   모델 호출 {}ms · 프롬프트 {} · 완성 {} 토큰",
                        traceId, elapsedMs, usage.getPromptTokens(), usage.getCompletionTokens());
            }
        }
        return response;
    }
}
