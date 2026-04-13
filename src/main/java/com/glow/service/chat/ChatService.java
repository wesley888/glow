package com.glow.service.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Module 1 — Basic Chat + Streaming.
 * <p>
 * Uses {@link ChatClientRouter} to select the appropriate model at runtime.
 * Pass {@code model} = "openai" / "kimi" / "deepseek" to switch providers.
 * Defaults to OpenAI when model is null or blank.
 */
@Service
public class ChatService {

    private final ChatClientRouter router;

    public ChatService(ChatClientRouter router) {
        this.router = router;
    }

    /**
     * Synchronous chat with conversation memory.
     *
     * @param model     provider name: "openai" | "kimi" | "deepseek" (null → OpenAI)
     * @param sessionId conversation ID for memory isolation
     */
    public String chat(String message, String sessionId, String model) {
        return router.route(model).prompt()
                .user(message)
                .advisors(a -> a.param("conversationId", sessionId))
                .call()
                .content();
    }

    /**
     * Streaming chat — returns a {@link Flux} of token chunks.
     */
    public Flux<String> chatStream(String message, String sessionId, String model) {
        return router.route(model).prompt()
                .user(message)
                .advisors(a -> a.param("conversationId", sessionId))
                .stream()
                .content();
    }

    /**
     * Stateless one-shot chat with a custom system prompt (no memory).
     */
    public String chatWithSystem(String systemPrompt, String userMessage, String model) {
        return router.route(model).prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}
