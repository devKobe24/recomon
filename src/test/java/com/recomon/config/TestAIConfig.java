package com.recomon.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * packageName    : com.recomon.config
 * fileName       : TestAIConfig
 * author         : kobe
 * date           : 2026. 1. 12.
 * description    : 테스트 환경용 VectorStore 설정
 *                  테스트에서는 SimpleVectorStore를 사용하여 PostgreSQL 연결 없이 테스트 가능
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 12.        kobe       최초 생성
 */
@TestConfiguration
public class TestAIConfig {

    @Bean
    @Primary
    @ConditionalOnBean(EmbeddingModel.class)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 테스트 환경에서는 SimpleVectorStore 사용 (메모리 기반)
        // EmbeddingModel은 Spring AI 자동 설정에 의해 생성됨
        return SimpleVectorStore.builder(embeddingModel)
                .build();
    }
}
