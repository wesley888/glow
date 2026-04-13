package com.glow.service.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Module 4 — Chat Memory / Conversation History.
 * <p>
 * Demonstrates:
 * <ol>
 *   <li>Per-session conversation memory</li>
 *   <li>Streaming with memory</li>
 *   <li>Inspecting and clearing conversation history</li>
 * </ol>
 * Window size is managed by the {@link org.springframework.ai.chat.memory.MessageWindowChatMemory}
 * bean configured in {@link com.glow.config.MemoryConfig}.
 */
@Service
public class MemoryChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public MemoryChatService(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * Chat with automatic conversation memory.
     */
    public String chat(String message, String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
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
                        .build())
                .stream()
                .content();
    }

    /**
     * Retrieve the conversation history for a session.
     */
    public List<Message> getHistory(String sessionId) {
        return chatMemory.get(sessionId);
    }

    /**
     * Clear all messages for a session (reset conversation).
     */
    public void clearHistory(String sessionId) {
        chatMemory.clear(sessionId);
    }
}
