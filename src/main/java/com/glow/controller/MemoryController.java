package com.glow.controller;

import com.glow.model.ChatRequest;
import com.glow.service.memory.MemoryChatService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Module 4 — Chat Memory API.
 * <pre>
 * POST   /api/memory/chat              — chat with persistent memory
 * POST   /api/memory/chat/stream       — streaming chat with memory
 * GET    /api/memory/history/{session} — view conversation history
 * DELETE /api/memory/history/{session} — clear conversation history
 * </pre>
 */
@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final MemoryChatService memoryChatService;

    public MemoryController(MemoryChatService memoryChatService) {
        this.memoryChatService = memoryChatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return memoryChatService.chat(request.message(), request.sessionId());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return memoryChatService.chatStream(request.message(), request.sessionId());
    }

    @GetMapping("/history/{sessionId}")
    public List<Message> getHistory(@PathVariable String sessionId) {
        return memoryChatService.getHistory(sessionId);
    }

    @DeleteMapping("/history/{sessionId}")
    public String clearHistory(@PathVariable String sessionId) {
        memoryChatService.clearHistory(sessionId);
        return "Conversation history cleared for session: " + sessionId;
    }
}
