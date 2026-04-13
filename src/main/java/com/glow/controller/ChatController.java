package com.glow.controller;

import com.glow.model.ChatRequest;
import com.glow.service.chat.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Module 1 — Chat API.
 * <p>
 * Endpoints:
 * <pre>
 * POST /api/chat              — synchronous single response
 * POST /api/chat/stream       — streaming (SSE) response
 * POST /api/chat/with-system  — custom system prompt
 * </pre>
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Synchronous chat with conversation memory.
     * Request body example:
     * <pre>{"message":"你好","sessionId":"s1","model":"kimi"}</pre>
     */
    @PostMapping
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request.message(), request.sessionId(), request.model());
    }

    /**
     * Streaming chat — tokens are pushed as SSE events.
     * Client: {@code EventSource} or {@code fetch} with stream reader.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatService.chatStream(request.message(), request.sessionId(), request.model());
    }

    /** One-shot chat with a caller-specified system prompt (no memory). */
    @PostMapping("/with-system")
    public String chatWithSystem(@RequestParam String system,
                                 @RequestParam String message,
                                 @RequestParam(defaultValue = "openai") String model) {
        return chatService.chatWithSystem(system, message, model);
    }

    /** List all supported model providers. */
    @GetMapping("/models")
    public java.util.List<String> listModels() {
        return java.util.List.of("openai", "kimi", "deepseek");
    }
}
