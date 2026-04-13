package com.glow.controller;

import com.glow.model.MovieRecommendation;
import com.glow.service.output.StructuredOutputService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module 3 — Structured Output API.
 * <pre>
 * GET /api/output/movie         — single entity output
 * GET /api/output/movies        — list of entities
 * GET /api/output/topics        — list of strings
 * GET /api/output/country       — key-value map
 * </pre>
 */
@RestController
@RequestMapping("/api/output")
public class OutputController {

    private final StructuredOutputService outputService;

    public OutputController(StructuredOutputService outputService) {
        this.outputService = outputService;
    }

    @GetMapping("/movie")
    public MovieRecommendation recommendMovie(@RequestParam(defaultValue = "sci-fi") String genre) {
        return outputService.recommendMovie(genre);
    }

    @GetMapping("/movies")
    public List<MovieRecommendation> recommendMovies(
            @RequestParam(defaultValue = "thriller") String genre,
            @RequestParam(defaultValue = "3") int count) {
        return outputService.recommendMovies(genre, count);
    }

    @GetMapping("/topics")
    public List<String> listTopics(@RequestParam String subject) {
        return outputService.listTopics(subject);
    }

    @GetMapping("/country")
    public Map<String, Object> getCountryInfo(@RequestParam String country) {
        return outputService.getCountryInfo(country);
    }
}
