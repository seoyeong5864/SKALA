package com.skala.ai.lab.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.skala.ai.lab.advisor.AuditAdvisor;
import com.skala.ai.lab.dto.ChatResponse;
import com.skala.ai.lab.tool.OrderTools;
import com.skala.ai.lab.tool.RefundTools;
import com.skala.ai.lab.tool.ToolCallLimiter;
import com.skala.ai.lab.tool.ToolInvocationTracker;

@Service
public class AgentService {

    private static final Logger audit = LoggerFactory.getLogger("AI_AUDIT");

    // 레드팀 8번(비용 공격) 대응 — 실습에서 실제로 걸어볼 수 있게 낮게 잡은 테스트용 값이지 운영값이 아니다.
    private static final int MAX_QUESTION_LENGTH = 2000;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final OrderTools orderTools;
    private final RefundTools refundTools;

    public AgentService(@Qualifier("agentChatClient") ChatClient agentChatClient,
                         ChatMemory chatMemory,
                         OrderTools orderTools,
                         RefundTools refundTools) {
        this.chatClient = agentChatClient;
        this.chatMemory = chatMemory;
        this.orderTools = orderTools;
        this.refundTools = refundTools;
    }

    public ChatResponse chat(String question, String userId, String sessionId) {
        // 근거 검색·모델 호출 전, 가장 먼저(가장 싸게) 거른다 — 토큰 한 푼 쓰기 전에 막는 게 핵심이다.
        if (question != null && question.length() > MAX_QUESTION_LENGTH) {
            throw new QuestionTooLongException(question.length(), MAX_QUESTION_LENGTH);
        }

        String conversationId = (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();
        // 여기서 만들어야 @Tool 메서드(ToolContext)까지 같은 traceId가 전달된다 —
        // advisor의 request.context()에 나중에 넣는 값은 .toolContext(...) 경로와 별개라 도구로 안 간다.
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        ToolInvocationTracker.reset();
        try {
            ChatClientResponse response = chatClient.prompt()
                    .user(question)
                    .tools(orderTools, refundTools)
                    // 사용자 ID는 프롬프트가 아니라 이 통로로 — 모델이 바꿔 부를 수 없다
                    // conversationId도 같이 실어 보낸다 — ToolAuditAspect가 도구 호출 상한을 세션 단위로 세려면 필요하다
                    .toolContext(Map.of(
                            "userId", userId,
                            AuditAdvisor.TRACE_ID, traceId,
                            ToolCallLimiter.CONTEXT_KEY, conversationId))
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .chatClientResponse();

            String answer = response.chatResponse().getResult().getOutput().getText();
            return new ChatResponse(answer, extractSources(response));
        } finally {
            ToolInvocationTracker.reset();
        }
    }

    // 모델이 답변 텍스트 안에 출처를 적었는지와 무관하게, 실제로 검색된 문서 메타데이터에서 직접 뽑는다.
    // 단, 이번 턴에 도구가 실제로 호출됐다면 근거가 우연히 검색됐더라도 출처를 붙이지 않는다 —
    // 이 문서 집합에서는 순수 주문 질문도 정책 문서와 유사도 임계값을 넘어서곤 해서, 유사도만으로는
    // "규정 질문인지"를 안정적으로 가릴 수 없다 (ToolInvocationTracker 참고).
    private List<String> extractSources(ChatClientResponse response) {
        Object raw = response.chatResponse().getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        List<Document> docs = raw instanceof List<?> list
                ? list.stream().filter(Document.class::isInstance).map(Document.class::cast).toList()
                : List.of();

        if (!docs.isEmpty()) {
            Object traceId = response.context().get(AuditAdvisor.TRACE_ID);
            String scores = docs.stream()
                    .map(d -> String.format("%.2f", d.getScore()))
                    .collect(java.util.stream.Collectors.joining("/"));
            audit.info("[{}]   검색 {}건({})", traceId, docs.size(), scores);
        }

        if (ToolInvocationTracker.wasInvoked()) {
            return List.of();
        }
        return docs.stream()
                .map(d -> String.valueOf(d.getMetadata().get("source")))
                .distinct()
                .toList();
    }

    // Step 4 검증용 — 차단이 메모리 저장보다 앞에 있는지 확인할 때 씀
    public List<String> history(String sessionId) {
        return chatMemory.get(sessionId).stream().map(Message::getText).toList();
    }
}
