package com.glow.controller;

import com.glow.service.tools.FunctionCallingService;
import org.springframework.web.bind.annotation.*;

/**
 * Module 6 — Function Calling / Tool Use API.
 * <pre>
 * GET /api/tools/weather    — LLM calls weather tool
 * GET /api/tools/datetime   — LLM calls date/time tool
 * GET /api/tools/calculate  — LLM delegates math to calculator tool
 * GET /api/tools/ask        — LLM picks from all tools automatically
 * </pre>
 */
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final FunctionCallingService functionCallingService;

    public ToolController(FunctionCallingService functionCallingService) {
        this.functionCallingService = functionCallingService;
    }

    @GetMapping("/weather")
    public String weather(@RequestParam String question) {
        return functionCallingService.askWeather(question);
    }

    @GetMapping("/datetime")
    public String datetime(@RequestParam String question) {
        return functionCallingService.askDateTime(question);
    }

    @GetMapping("/calculate")
    public String calculate(@RequestParam String expression) {
        return functionCallingService.calculate(expression);
    }

    /** Multi-tool: LLM decides which tools to use. */
    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return functionCallingService.askAll(question);
    }
}
