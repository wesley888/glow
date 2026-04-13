package com.glow.service.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Routes a model name string to the corresponding {@link ChatClient} bean.
 * <p>
 * Supported values for {@code modelName}:
 * <ul>
 *   <li>{@code openai}   → default ChatClient (GPT-4o)</li>
 *   <li>{@code kimi}     → kimiChatClient (moonshot-v1-*)</li>
 *   <li>{@code deepseek} → deepSeekChatClient</li>
 * </ul>
 * Falls back to the primary (OpenAI) client if the requested model is not
 * configured or its API key is missing.
 */
@Slf4j
@Component
public class ChatClientRouter {

    private final ChatClient defaultChatClient;
    private final ApplicationContext context;

    public ChatClientRouter(@Qualifier("defaultChatClient") ChatClient defaultChatClient,
                            ApplicationContext context) {
        this.defaultChatClient = defaultChatClient;
        this.context = context;
    }

    /**
     * Returns the {@link ChatClient} for the given model name.
     * Falls back to OpenAI if the model is not available.
     */
    public ChatClient route(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return defaultChatClient;
        }
        return switch (modelName.toLowerCase().trim()) {
            case "kimi", "moonshot" -> resolve("kimiChatClient");
            case "deepseek"         -> resolve("deepSeekChatClient");
            case "openai", "gpt"    -> defaultChatClient;
            default -> {
                log.warn("[ROUTER] Unknown model '{}', falling back to OpenAI", modelName);
                yield defaultChatClient;
            }
        };
    }

    private ChatClient resolve(String beanName) {
        return Optional.ofNullable(context.containsBean(beanName)
                        ? context.getBean(beanName, ChatClient.class) : null)
                .orElseGet(() -> {
                    log.warn("[ROUTER] Bean '{}' not configured (missing API key?), falling back to OpenAI", beanName);
                    return defaultChatClient;
                });
    }
}
