package com.glow.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Module 7 — Logging Advisor.
 * Logs request start and elapsed time for both sync and streaming calls.
 * Implements {@link CallAdvisor} and {@link StreamAdvisor} (Spring AI 1.0.0 GA API).
 */
@Slf4j
@Component
public class LoggingAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();
        log.debug("[CHAT-REQ] call started");
        ChatClientResponse response = chain.nextCall(request);
        log.debug("[CHAT-RSP] elapsed={}ms", System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long start = System.currentTimeMillis();
        log.debug("[STREAM-REQ] stream started");
        return chain.nextStream(request)
                .doOnComplete(() -> log.debug("[STREAM-RSP] completed in {}ms", System.currentTimeMillis() - start));
    }
}
