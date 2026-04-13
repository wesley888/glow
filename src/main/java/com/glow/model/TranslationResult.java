package com.glow.model;

/**
 * Structured output for the translation prompt demo — Module 2 + 3.
 */
public record TranslationResult(
        String originalText,
        String translatedText,
        String sourceLanguage,
        String targetLanguage,
        double confidenceScore
) {}
