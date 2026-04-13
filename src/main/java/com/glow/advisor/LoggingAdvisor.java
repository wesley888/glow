package com.glow.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Module 7 — Logging Advisor.
 * <p>
 * Intercepts every chat request/response (both sync and streaming)
 * and logs the user input, model used, and response duration.
 * Implements both {@link CallAroundAdvisor} and {@link StreamAroundAdvisor}.
 */
@Slf4j
@Component
public class LoggingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // runs last so it wraps everything
    }

    // ===== Sync =====

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        long start = System.currentTimeMillis();
        log.debug("[CHAT-REQ] user=\"{}\"", truncate(request.userText()));

        AdvisedResponse response = chain.nextAroundCall(request);

        long elapsed = System.currentTimeMillis() - start;
        String content = extractContent(response);
        log.debug("[CHAT-RSP] elapsed={}ms content=\"{}\"", elapsed, truncate(content));

        return response;
    }

    // ===== Streaming =====

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest request, StreamAroundAdvisorChain chain) {
        log.debug("[STREAM-REQ] user=\"{}\"", truncate(request.userText()));
        long start = System.currentTimeMillis();

        return chain.nextAroundStream(request)
                .doOnComplete(() -> {
                    long elapsed = System.currentTimeMillis() - start;
                    log.debug("[STREAM-RSP] completed in {}ms", elapsed);
                });
    }

    // ===== Helpers =====

    private String extractContent(AdvisedResponse response) {
        try {
            return response.response().getResult().getOutput().getText();
        } catch (Exception e) {
            return "(unable to extract content)";
        }
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }
}
