package com.glow.service.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Module 4 — Chat Memory / Conversation History.
 * <p>
 * Demonstrates:
 * <ol>
 *   <li>Per-session conversation memory with configurable window size</li>
 *   <li>Streaming with memory</li>
 *   <li>Inspecting and clearing conversation history</li>
 *   <li>Custom memory window per conversation</li>
 * </ol>
 */
@Service
public class MemoryChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final int defaultWindowSize;

    public MemoryChatService(ChatClient chatClient,
                             ChatMemory chatMemory,
                             @Value("${glow.chat.memory.window-size:20}") int defaultWindowSize) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.defaultWindowSize = defaultWindowSize;
    }

    /**
     * Chat with automatic conversation memory.
     * The MessageChatMemoryAdvisor injects the last N messages from chatMemory
     * into the prompt before each call, and saves the new exchange after.
     */
    public String chat(String message, String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .maxMessages(defaultWindowSize)
                        .build())
                .call()
                .content();
    }

    /**
     * Streaming chat with memory.
     */
    public Flux<String> chatStream(String message, String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .maxMessages(defaultWindowSize)
                        .build())
                .stream()
                .content();
    }

    /**
     * Chat with a custom memory window — override the default window size per call.
     */
    public String chatWithWindow(String message, String sessionId, int windowSize) {
        return chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .maxMessages(windowSize)
                        .build())
                .call()
                .content();
    }

    /**
     * Retrieve the full conversation history for a session.
     */
    public List<Message> getHistory(String sessionId) {
        return chatMemory.get(sessionId, Integer.MAX_VALUE);
    }

    /**
     * Clear all messages for a session (reset conversation).
     */
    public void clearHistory(String sessionId) {
        chatMemory.clear(sessionId);
    }
}
