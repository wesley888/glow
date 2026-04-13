package com.glow.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * Module 7 — Safety / Content-Filter Advisor.
 * <p>
 * Runs <em>before</em> the request reaches the model.
 * Blocks requests that contain configured sensitive keywords and
 * short-circuits with a safe refusal message — no API call is made.
 */
@Slf4j
@Component
public class SafetyAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private final List<String> blockedKeywords;

    public SafetyAdvisor(@Value("${glow.safety.blocked-keywords:}") String keywordsConfig) {
        this.blockedKeywords = Arrays.stream(keywordsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public String getName() {
        return "SafetyAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // runs first — must intercept before memory/logging
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        String blocked = findBlockedKeyword(request.userText());
        if (blocked != null) {
            log.warn("[SAFETY] Request blocked — keyword=\"{}\"", blocked);
            return buildRefusalResponse(request);
        }
        return chain.nextAroundCall(request);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest request, StreamAroundAdvisorChain chain) {
        String blocked = findBlockedKeyword(request.userText());
        if (blocked != null) {
            log.warn("[SAFETY] Stream request blocked — keyword=\"{}\"", blocked);
            return Flux.just(buildRefusalResponse(request));
        }
        return chain.nextAroundStream(request);
    }

    // ===== Helpers =====

    private String findBlockedKeyword(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        return blockedKeywords.stream()
                .filter(kw -> lower.contains(kw.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    private AdvisedResponse buildRefusalResponse(AdvisedRequest request) {
        AssistantMessage refusal = new AssistantMessage(
                "I'm sorry, I can't help with that request. Please try a different question.");
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(refusal)));
        return new AdvisedResponse(chatResponse, request.adviseContext());
    }
}
