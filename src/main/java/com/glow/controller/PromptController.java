package com.glow.controller;

import com.glow.model.TranslationResult;
import com.glow.service.prompt.PromptService;
import org.springframework.web.bind.annotation.*;

/**
 * Module 2 — Prompt Engineering API.
 * <pre>
 * POST /api/prompt/summarize          — template with variables
 * POST /api/prompt/translate          — classpath template + structured output
 * POST /api/prompt/sentiment          — few-shot classification
 * GET  /api/prompt/expert             — role-based system prompt
 * </pre>
 */
@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping("/summarize")
    public String summarize(@RequestParam String text,
                            @RequestParam(defaultValue = "English") String language,
                            @RequestParam(defaultValue = "100") int maxWords) {
        return promptService.summarize(text, language, maxWords);
    }

    @PostMapping("/translate")
    public TranslationResult translate(@RequestParam String text,
                                       @RequestParam String targetLanguage) {
        return promptService.translate(text, targetLanguage);
    }

    @PostMapping("/sentiment")
    public String sentiment(@RequestBody String text) {
        return promptService.classifySentiment(text);
    }

    @GetMapping("/expert")
    public String expertAnswer(@RequestParam String domain,
                               @RequestParam String question) {
        return promptService.expertAnswer(domain, question);
    }
}
