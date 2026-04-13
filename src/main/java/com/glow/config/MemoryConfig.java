package com.glow.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chat memory configuration.
 * <p>
 * {@link InMemoryChatMemory} is suitable for single-node dev/demo.
 * For multi-node production, replace with a persistent implementation
 * backed by Redis or a relational database.
 */
@Configuration
public class MemoryConfig {

    @Bean
    ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
