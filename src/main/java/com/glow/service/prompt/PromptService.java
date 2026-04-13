package com.glow.service.prompt;

import com.glow.model.TranslationResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Module 2 — Prompt Engineering.
 * <p>
 * Demonstrates:
 * <ol>
 *   <li>Loading prompt templates from classpath (.st files)</li>
 *   <li>Variable injection via {@link PromptTemplate}</li>
 *   <li>Few-shot examples injected into the system message</li>
 *   <li>Role-based message construction (System + User)</li>
 * </ol>
 */
@Service
public class PromptService {

    private final ChatClient chatClient;

    @Value("classpath:prompts/translation.st")
    private Resource translationPromptResource;

    @Value("classpath:prompts/rag-system.st")
    private Resource ragSystemPromptResource;

    public PromptService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 1. Simple template with variable substitution.
     * Template: "Summarize the following {language} text in {maxWords} words or fewer: {text}"
     */
    public String summarize(String text, String language, int maxWords) {
        PromptTemplate template = new PromptTemplate(
                "Summarize the following {language} text in {maxWords} words or fewer:\n\n{text}"
        );
        String rendered = template.render(Map.of(
                "language", language,
                "maxWords", maxWords,
                "text", text
        ));
        return chatClient.prompt()
                .user(rendered)
                .call()
                .content();
    }

    /**
     * 2. Template loaded from classpath file + structured output.
     * Demonstrates combining PromptTemplate with BeanOutputConverter.
     */
    public TranslationResult translate(String text, String targetLanguage) {
        PromptTemplate template = new PromptTemplate(translationPromptResource);
        String rendered = template.render(Map.of(
                "text", text,
                "targetLanguage", targetLanguage
        ));
        return chatClient.prompt()
                .user(rendered)
                .call()
                .entity(TranslationResult.class);
    }

    /**
     * 3. Few-shot prompting — inject examples into the system message.
     */
    public String classifySentiment(String text) {
        String systemWithExamples = """
                You are a sentiment classifier. Respond with exactly one word: POSITIVE, NEGATIVE, or NEUTRAL.

                Examples:
                User: "I love this product!"  → POSITIVE
                User: "This is terrible."      → NEGATIVE
                User: "The package arrived."   → NEUTRAL
                """;
        return chatClient.prompt()
                .system(systemWithExamples)
                .user(text)
                .call()
                .content();
    }

    /**
     * 4. Dynamic role-based conversation construction.
     * Builds a System + User message pair at runtime.
     */
    public String expertAnswer(String domain, String question) {
        return chatClient.prompt()
                .system("You are a world-class expert in {domain}. Answer precisely and cite sources where possible."
                        .replace("{domain}", domain))
                .user(question)
                .call()
                .content();
    }
}
