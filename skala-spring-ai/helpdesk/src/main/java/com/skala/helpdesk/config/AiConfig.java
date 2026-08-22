package com.skala.helpdesk.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

import com.skala.helpdesk.advisor.AuditAdvisor;
import com.skala.helpdesk.advisor.SafetyAdvisor;
import com.skala.helpdesk.advisor.TokenMeterAdvisor;
import com.skala.helpdesk.rag.IngestService;
import com.skala.helpdesk.tools.OrderTools;
import com.skala.helpdesk.tools.TicketTools;

@Configuration
public class AiConfig {

    @Value("classpath:/prompts/system.st")
    private Resource systemPromptResource;

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // 기동 완료 직후 docs/ 전체를 한 번 인제스트한다
    @Bean
    ApplicationRunner ingestRunner(IngestService ingestService) {
        return args -> ingestService.ingestAll();
    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository repo, HelpDeskProperties props) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repo) // JDBC — 재시작에도 유지
                .maxMessages(props.memory().max())
                .build();
    }

    @Bean
    ChatClient helpDeskClient(ChatClient.Builder builder, VectorStore vs,
                               ChatMemory memory, HelpDeskProperties props,
                               AuditAdvisor audit, TokenMeterAdvisor meter, SafetyAdvisor safety,
                               OrderTools orderTools, TicketTools ticketTools) throws IOException {
        String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);

        return builder.defaultSystem(systemPrompt)
                .defaultAdvisors(
                        audit, meter, safety,
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("주민등록번호", "카드번호"))
                                .failureResponse("민감한 개인정보가 포함된 요청은 처리할 수 없습니다. 다른 방식으로 다시 말씀해 주세요.")
                                .order(Ordered.HIGHEST_PRECEDENCE + 60)
                                .build(),
                        MessageChatMemoryAdvisor.builder(memory)
                                .order(Ordered.HIGHEST_PRECEDENCE + 200)
                                .build(),
                        QuestionAnswerAdvisor.builder(vs)
                                .searchRequest(SearchRequest.builder()
                                        .topK(props.rag().topK())
                                        .similarityThreshold(props.rag().threshold())
                                        .build())
                                .order(Ordered.HIGHEST_PRECEDENCE + 250)
                                .build())
                .defaultTools(orderTools, ticketTools)
                .build();
    }
}
