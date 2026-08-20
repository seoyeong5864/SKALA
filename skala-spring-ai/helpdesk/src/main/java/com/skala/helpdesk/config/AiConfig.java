package com.skala.helpdesk.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.skala.helpdesk.advisor.AuditAdvisor;
import com.skala.helpdesk.advisor.TokenMeterAdvisor;

@Configuration
public class AiConfig {

    @Value("classpath:/prompts/system.st")
    private Resource systemPromptResource;

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    ChatMemory chatMemory(HelpDeskProperties props) {
        return MessageWindowChatMemory.builder()
                .maxMessages(props.memory().max())
                .build();
    }

    @Bean
    ChatClient helpDeskClient(ChatClient.Builder builder, VectorStore vs,
                               ChatMemory memory, HelpDeskProperties props,
                               AuditAdvisor audit, TokenMeterAdvisor meter) throws IOException {
        String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
        Advisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vs)
                        .topK(props.rag().topK())
                        .similarityThreshold(props.rag().threshold())
                        .build())
                .build();

        return builder.defaultSystem(systemPrompt)
                .defaultAdvisors(
                        audit, meter,
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("주민등록번호", "카드번호"))
                                .build(),
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        retrievalAdvisor)
                .build();
    }
}
