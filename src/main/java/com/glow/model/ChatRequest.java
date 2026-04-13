package com.glow.model;

/**
 * Generic chat request payload.
 *
 * @param message    the user's message
 * @param sessionId  conversation ID for memory isolation (optional)
 */
public record ChatRequest(String message, String sessionId) {

    public ChatRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "default";
        }
    }
}
