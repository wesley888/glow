package com.glow.advisor;

import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Module 7 — Safety / Content-Filter Advisor.
 * <p>
 * Delegates to Spring AI's built-in {@link SafeGuardAdvisor} which blocks requests
 * containing configured sensitive keywords and returns a safe refusal message.
 * Configure keywords via {@code glow.safety.blocked-keywords} (comma-separated).
 */
@Configuration
public class SafetyAdvisor {

    @Bean
    SafeGuardAdvisor safeGuardAdvisor(
            @Value("${glow.safety.blocked-keywords:}") String keywordsConfig) {
        List<String> words = Arrays.stream(keywordsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        SafeGuardAdvisor.Builder builder = SafeGuardAdvisor.builder()
                .failureResponse("I'm sorry, I can't help with that request. Please try a different question.");
        if (!words.isEmpty()) {
            builder.sensitiveWords(words);
        }
        return builder.build();
    }
}
