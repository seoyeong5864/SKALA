package com.skala.ai.lab.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.skala.ai.lab.dto.AnswerDto;

@Service
public class RagService {

    // "우주 배송"처럼 키워드만 겹치는 트랩은 여기서 못 거르므로, 완전히 무관한 질문만 걸러내는 낮은 값으로 둔다.
    // 애매한 트랩은 system 프롬프트의 거절 지시에 맡긴다.
    private static final double SIMILARITY_THRESHOLD = 0.3;

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, @Qualifier("ragChatClient") ChatClient ragChatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = ragChatClient;
    }

    public AnswerDto ask(String question) {
        return ask(question, null, null);
    }

    public AnswerDto ask(String question, String dept, String sessionId) {
        List<Document> docs = retrieve(question, 4, dept);
        if (docs.isEmpty()) {
            return AnswerDto.unknown(); // 근거가 없으면 모델을 부르지 않는다
        }

        // sessionId가 없으면 매번 새 대화로 취급해, 서로 다른 요청의 기록이 섞이지 않게 한다
        String conversationId = (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();

        return chatClient.prompt()
                .system("""
                    아래 [근거]만 사용해 답한다. 근거에 없으면 "확인할 수 없습니다"라고 답한다.
                    추측하지 않는다. 답변 끝에 사용한 출처를 [출처: 파일명] 형식으로 남긴다.
                    """)
                .user(u -> u.text("[근거]\n{context}\n\n[질문] {question}")
                        .param("context", format(docs))
                        .param("question", question))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .entity(AnswerDto.class); // 구조화 출력 — 문자열 파싱 금지
    }

    private List<Document> retrieve(String question, int topK, String dept) {
        SearchRequest.Builder request = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(SIMILARITY_THRESHOLD);

        if (dept != null && !dept.isBlank()) {
            request.filterExpression(new FilterExpressionBuilder().eq("dept", dept).build());
        }

        return vectorStore.similaritySearch(request.build());
    }

    private String format(List<Document> docs) {
        return docs.stream()
                .map(d -> "[출처: %s]\n%s".formatted(d.getMetadata().get("source"), d.getText()))
                .collect(Collectors.joining("\n\n"));
    }
}
