package com.skala.ai.lab.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.skala.ai.lab.advisor.AuditAdvisor;
import com.skala.ai.lab.advisor.SafetyAdvisor;
import com.skala.ai.lab.advisor.TokenMeterAdvisor;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class ChatClientConfig {

    // QuestionAnswerAdvisor 기본 템플릿은 "근거에 없으면 모른다고 답하라"고 강제해,
    // 도구로 답해야 할 질문(주문 조회 등)까지 거절하게 만든다.
    // 근거가 비었을 때는 이 섹션을 무시하고 도구·일반 지식으로 넘어가도록 직접 템플릿을 준다.
    // (출처 표기는 이 템플릿 지시가 아니라 AgentService가 RETRIEVED_DOCUMENTS 메타데이터로 코드에서 강제한다.)
    private static final PromptTemplate RAG_PROMPT_TEMPLATE = new PromptTemplate("""
            {query}

            아래는 벡터 검색으로 찾은 규정 근거다. 비어 있을 수도 있다.
            근거가 있으면 그 내용만 사용해 답한다. 비어 있으면 이 섹션은 무시하고,
            필요하면 도구를 사용하거나 아는 대로 답한다.

            [근거]
            {question_answer_context}
            """);

    // 인메모리 대화 저장소 — 최근 20개 메시지까지만 유지
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    // 시스템 프롬프트는 질문마다 근거(context)를 넣어 RagService에서 매번 다르게 구성하므로
    // 여기서는 고정 옵션과 대화 메모리 advisor만 설정한다.
    // 대화 ID는 요청마다 RagService가 advisors(a -> a.param(...))로 지정한다
    @Bean
    ChatClient ragChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
            .defaultOptions(ChatOptions.builder()
                .temperature(0.0))
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }

    // DAY3 — 규정(RAG)과 주문(도구)을 함께 다루는 상담 에이전트용 클라이언트.
    // 공통으로 해야 할 일(차단·기억·근거 검색·계측)은 각각의 Advisor로 모으고, 순서를 명시적으로 고정한다.
    //
    // 주의: Spring AI가 .tools(...)를 쓰면 ToolCallingAdvisor를 자동 등록하는데, 이게 기본적으로
    // order=HIGHEST_PRECEDENCE+300 으로 아주 바깥쪽에 위치해 도구 호출 왕복(라운드트립) 전체를 감싼다.
    // Audit·Safety·Memory·QuestionAnswer를 이보다 작은(더 바깥) order로 두지 않으면, 도구를 여러 번
    // 호출하는 동안 이 advisor들이 매 라운드마다 재실행돼 근거가 중복 주입되고 메모리에 중간 상태까지 저장된다.
    // 그래서 HIGHEST_PRECEDENCE 기준으로 값을 잡는다 — 슬라이드의 0/100/200/300은 상대적인 개념만 차용한다.
    // 차단(60)이 기억(200)보다 앞에 있어야 한다 — 순서가 뒤바뀌면 차단된 문장도 메모리에 남는다.
    @Bean
    ChatClient agentChatClient(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory,
                                MeterRegistry meterRegistry) {
        return builder
            .defaultSystem("""
                너는 이커머스 상담 에이전트다.
                - 주문 관련 질문은 반드시 도구를 호출해 확인한 정보로만 답한다. 도구 없이 주문 상태를 추측하지 않는다.
                - 환불 요청은 requestRefund 도구로 접수만 한다. 네가 직접 환불을 처리했다고 말하지 않는다 — 담당자 승인 후 처리된다고 안내한다.
                - 존댓말을 쓰고 간결하게 답한다.
                - 주문번호가 없으면 먼저 물어본다.
                """)
            .defaultOptions(ChatOptions.builder()
                .temperature(0.2))
            .defaultAdvisors(
                new AuditAdvisor(meterRegistry),                       // HIGHEST_PRECEDENCE+10  가장 바깥 — traceId 발급, 요청/응답 로그
                new SafetyAdvisor(),                                   // HIGHEST_PRECEDENCE+60  차단
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .order(Ordered.HIGHEST_PRECEDENCE + 200)           // 기억 — 도구 루프당 한 번만
                    .build(),
                QuestionAnswerAdvisor.builder(vectorStore)             // 근거 검색 — 도구 루프 시작 전 한 번만
                    .searchRequest(SearchRequest.builder().topK(4).similarityThreshold(0.3).build())
                    .promptTemplate(RAG_PROMPT_TEMPLATE)
                    .order(Ordered.HIGHEST_PRECEDENCE + 250)           // ToolCallingAdvisor(+300)보다 바깥
                    .build(),
                new TokenMeterAdvisor(meterRegistry))                  // order 900 — 도구 왕복마다 토큰 지표·로그
            .build();
    }
}
