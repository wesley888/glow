package com.glow.model;

import java.util.List;

/**
 * Structured output demo — Module 3.
 * Spring AI will deserialize the LLM JSON response directly into this record.
 */
public record MovieRecommendation(
        String title,
        String director,
        int year,
        String genre,
        double rating,
        String reason,
        List<String> similarMovies
) {}
