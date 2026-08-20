package com.skala.ai.lab.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import com.skala.ai.lab.service.IngestService;

@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore simplVectorStore(EmbeddingModel embeddingModel){
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // 인메모리 스토어라 기동마다 비어 있으므로, 기동 완료 직후 한 번 전체 인제스트를 실행한다
    @Bean
    public ApplicationRunner ingestRunner(IngestService ingestService) {
        return args -> ingestService.ingestAll();
    }
}
