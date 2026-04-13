package com.glow.config;

import com.glow.advisor.LoggingAdvisor;
import com.glow.advisor.SafetyAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

/**
 * Central AI configuration.
 * <p>
 * Creates the primary {@link ChatClient} with default system prompt,
 * memory advisor, logging advisor, and safety advisor already wired in.
 * Individual services can build their own specialized clients from this
 * bean via {@code chatClient.mutate()}.
 */
@Configuration
public class AiConfig {

    /** Default system prompt loaded from classpath:prompts/chat-system.st */
    @Value("classpath:prompts/chat-system.st")
    private Resource systemPromptResource;

    /**
     * Primary ChatClient — used by most services.
     * Advisors applied in order: Safety → Memory → Logging.
     */
    @Bean
    @Primary
    ChatClient defaultChatClient(ChatModel chatModel,
                                 ChatMemory chatMemory,
                                 SafetyAdvisor safetyAdvisor,
                                 LoggingAdvisor loggingAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPromptResource)
                .defaultAdvisors(
                        safetyAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        loggingAdvisor
                )
                .build();
    }

    /**
     * SimpleVectorStore — in-memory vector store for development.
     * Switch to PgVectorStore for production by uncommenting the pgvector
     * starter in pom.xml and the vectorstore config in application.yml.
     */
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
