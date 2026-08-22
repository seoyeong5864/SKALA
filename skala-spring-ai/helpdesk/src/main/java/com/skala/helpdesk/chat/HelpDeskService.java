package com.skala.helpdesk.chat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import com.skala.helpdesk.chat.AnswerDto.Source;
import com.skala.helpdesk.config.HelpDeskProperties;
import com.skala.helpdesk.tools.ToolCallLimiter;
import com.skala.helpdesk.tools.ToolInvocationTracker;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class HelpDeskService {

    private static final Logger log = LoggerFactory.getLogger(HelpDeskService.class);
    private static final String TENANT = "helpdesk";
    private static final String FALLBACK_TEXT = "일시적으로 응답할 수 없습니다. 잠시 후 다시 시도해 주세요.";

    private final ChatClient chat;
    private final MeterRegistry registry;
    private final HelpDeskProperties props;
    private final Map<String, List<Source>> lastSources = new ConcurrentHashMap<>();

    public HelpDeskService(ChatClient helpDeskClient, MeterRegistry registry, HelpDeskProperties props) {
        this.chat = helpDeskClient;
        this.registry = registry;
        this.props = props;
    }

    public AnswerDto ask(String question, String userId, String sessionId) {
        checkLength(question);
        String conversationId = conversationId(TENANT, userId, sessionId);

        ToolInvocationTracker.reset();
        try {
            ChatClientResponse response;
            boolean toolUsed;
            try {
                response = chat.prompt()
                        .user(question)
                        .toolContext(Map.of("userId", userId, ToolCallLimiter.CONTEXT_KEY, conversationId))
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .call()
                        .chatClientResponse();
            } catch (Exception e) {
                // 주 모델 장애 시에도 대화 계속 - 폴백 메시지로 응답을 이어감
                log.error("모델 호출 실패 conversationId={}", conversationId, e);
                return new AnswerDto(FALLBACK_TEXT, List.of(), false);
            }

            toolUsed = ToolInvocationTracker.wasInvoked();
           
            ToolInvocationTracker.invoked().forEach(tool ->
                    registry.counter("ai.tool.calls", "tool", tool).increment());

            List<Source> sources = toolUsed ? List.of() : extractSources(response);
            String text = response.chatResponse().getResult().getOutput().getText();
            return new AnswerDto(text, sources, toolUsed);
        } finally {
            ToolInvocationTracker.reset();
        }
    }

    public Flux<String> stream(String question, String userId, String sessionId) {
        checkLength(question);
        String conversationId = conversationId(TENANT, userId, sessionId);

        return chat.prompt()
                .user(question)
                .toolContext(Map.of("userId", userId, ToolCallLimiter.CONTEXT_KEY, conversationId))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .chatClientResponse()
                .doOnNext(r -> lastSources.put(conversationId, extractSources(r)))
                .mapNotNull(r -> {
                    var result = r.chatResponse() != null ? r.chatResponse().getResult() : null;
                    return result != null && result.getOutput() != null ? result.getOutput().getText() : null;
                })
                .filter(text -> !text.isEmpty())
                .onErrorResume(e -> {
                    // 주 모델 장애 시에도 스트림을 에러로 끊지 않고 폴백 메시지 한 줄로 마무리
                    log.error("스트리밍 모델 호출 실패 conversationId={}", conversationId, e);
                    return Flux.just(FALLBACK_TEXT);
                });
    }

    public List<Source> lastSources(String userId, String sessionId) {
        return lastSources.getOrDefault(conversationId(TENANT, userId, sessionId), List.of());
    }

    public String conversationId(String tenantId, String userId, String sessionId) {
        return "%s:%s:%s".formatted(tenantId, userId, sessionId);
    }

    private void checkLength(String question) {
        int max = props.chat().maxQuestionLength();
        if (question != null && question.length() > max) {
            throw new QuestionTooLongException(question.length(), max);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Source> extractSources(ChatClientResponse response) {
        List<Document> used = (List<Document>) response.context()
                .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        return used == null ? List.of() : used.stream()
                .map(d -> new Source((String) d.getMetadata().get("source"),
                                      (String) d.getMetadata().get("version")))
                .distinct()
                .toList();
    }
}
