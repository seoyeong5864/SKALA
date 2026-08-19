package com.skala.ai.lab.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

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
}
