package com.glow.model;

/**
 * Generic chat request payload.
 *
 * @param message    the user's message
 * @param sessionId  conversation ID for memory isolation (optional, defaults to "default")
 * @param model      model provider: "openai" | "kimi" | "deepseek" (optional, defaults to "openai")
 */
public record ChatRequest(String message, String sessionId, String model) {

    public ChatRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "default";
        }
        if (model == null || model.isBlank()) {
            model = "openai";
        }
    }
}
