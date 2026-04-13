package com.glow.service.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Module 1 — Basic Chat + Streaming.
 * <p>
 * Uses the pre-configured primary {@link ChatClient} which already has:
 * <ul>
 *   <li>Default system prompt (chat-system.st)</li>
 *   <li>Memory advisor (conversation history)</li>
 *   <li>Logging + Safety advisors</li>
 * </ul>
 */
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Synchronous single-turn chat.
     * The memory advisor automatically injects previous turns for the given sessionId.
     */
    public String chat(String message, String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param("conversationId", sessionId))
                .call()
                .content();
    }

    /**
     * Streaming chat — returns a {@link Flux} of token chunks.
     * Controller should produce {@code text/event-stream}.
     */
    public Flux<String> chatStream(String message, String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param("conversationId", sessionId))
                .stream()
                .content();
    }

    /**
     * Stateless one-shot chat with a custom system prompt.
     * No memory or advisors — useful for isolated single-turn tasks.
     */
    public String chatWithSystem(String systemPrompt, String userMessage) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}
