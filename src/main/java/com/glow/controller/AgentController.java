package com.glow.controller;

import com.glow.service.agent.AgentService;
import org.springframework.web.bind.annotation.*;

/**
 * Module 8 — Agent API.
 * <pre>
 * POST /api/agent/run         — automatic agent (Spring AI drives the loop)
 * POST /api/agent/react       — manual ReAct agent (caller controls iterations)
 * </pre>
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /** Fully automatic agent — Spring AI handles tool-call loop. */
    @PostMapping("/run")
    public String runAutoAgent(@RequestParam String task) {
        return agentService.runAutoAgent(task);
    }

    /**
     * Manual ReAct agent — returns answer + intermediate tool-call steps.
     * Useful for debugging agent reasoning or adding human-in-the-loop logic.
     */
    @PostMapping("/react")
    public AgentService.AgentResult runReActAgent(
            @RequestParam String task,
            @RequestParam(defaultValue = "10") int maxIterations) {
        return agentService.runReActAgent(task, maxIterations);
    }
}
