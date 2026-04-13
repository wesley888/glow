package com.glow.config;

import com.glow.advisor.LoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Multi-model configuration.
 * <p>
 * Each provider that is compatible with the OpenAI API format can be wired up
 * here by pointing {@link OpenAiApi} to a different {@code base-url}.
 * Currently supports:
 * <ul>
 *   <li><b>Kimi</b> (Moonshot AI) — {@code KIMI_API_KEY} env var</li>
 *   <li><b>DeepSeek</b>           — {@code DEEPSEEK_API_KEY} env var</li>
 * </ul>
 * Add more providers by following the same pattern.
 */
@Configuration
public class MultiModelConfig {

    @Value("classpath:prompts/chat-system.st")
    private Resource systemPromptResource;

    // ===== Kimi (Moonshot AI) =====

    /**
     * Activated only when {@code glow.models.kimi.api-key} is configured.
     */
    @Bean("kimiChatClient")
    @ConditionalOnProperty(prefix = "glow.models.kimi", name = "api-key", matchIfMissing = false)
    ChatClient kimiChatClient(
            @Value("${glow.models.kimi.api-key}") String apiKey,
            @Value("${glow.models.kimi.base-url}") String baseUrl,
            @Value("${glow.models.kimi.model}") String model,
            ChatMemory chatMemory,
            SafeGuardAdvisor safeGuardAdvisor,
            LoggingAdvisor loggingAdvisor) {

        OpenAiApi kimiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatModel kimiModel = OpenAiChatModel.builder()
                .openAiApi(kimiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(0.7)
                        .build())
                .build();

        return ChatClient.builder(kimiModel)
                .defaultSystem(systemPromptResource)
                .defaultAdvisors(
                        safeGuardAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        loggingAdvisor)
                .build();
    }

    // ===== DeepSeek =====

    @Bean("deepSeekChatClient")
    @ConditionalOnProperty(prefix = "glow.models.deepseek", name = "api-key", matchIfMissing = false)
    ChatClient deepSeekChatClient(
            @Value("${glow.models.deepseek.api-key}") String apiKey,
            @Value("${glow.models.deepseek.base-url}") String baseUrl,
            @Value("${glow.models.deepseek.model}") String model,
            ChatMemory chatMemory,
            SafeGuardAdvisor safeGuardAdvisor,
            LoggingAdvisor loggingAdvisor) {

        OpenAiApi deepSeekApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatModel deepSeekModel = OpenAiChatModel.builder()
                .openAiApi(deepSeekApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(0.7)
                        .build())
                .build();

        return ChatClient.builder(deepSeekModel)
                .defaultSystem(systemPromptResource)
                .defaultAdvisors(
                        safeGuardAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        loggingAdvisor)
                .build();
    }
}
