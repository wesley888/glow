package com.glow.service.agent;

import com.glow.tools.CalculatorTools;
import com.glow.tools.DateTimeTools;
import com.glow.tools.WeatherTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 8 — AI Agent.
 * <p>
 * Two agent patterns:
 * <ol>
 *   <li><b>Automatic Agent (ChatClient)</b> — Spring AI drives the tool loop.
 *       The model calls tools, gets results, and continues until it produces
 *       a final answer. Zero boilerplate.</li>
 *   <li><b>Manual ReAct Agent</b> — caller controls the loop. Useful when
 *       you need to inspect intermediate steps, add human-in-the-loop
 *       confirmation, or limit iteration count.</li>
 * </ol>
 */
@Slf4j
@Service
public class AgentService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final WeatherTools weatherTools;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;

    public AgentService(ChatClient chatClient,
                        ChatModel chatModel,
                        WeatherTools weatherTools,
                        DateTimeTools dateTimeTools,
                        CalculatorTools calculatorTools) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
        this.weatherTools = weatherTools;
        this.dateTimeTools = dateTimeTools;
        this.calculatorTools = calculatorTools;
    }

    /**
     * 1. Automatic agent — simplest form.
     * Spring AI's ChatClient handles the tool-call → execute → continue loop.
     */
    public String runAutoAgent(String task) {
        log.info("[AGENT-AUTO] Task: {}", task);
        return chatClient.prompt()
                .system("""
                        You are an autonomous agent. Break down tasks, use available tools,
                        and provide a comprehensive final answer. Always verify your results.
                        """)
                .tools(weatherTools, dateTimeTools, calculatorTools)
                .user(task)
                .call()
                .content();
    }

    /**
     * 2. Manual ReAct (Reasoning + Acting) agent loop.
     * <p>
     * Each iteration:
     * <ol>
     *   <li>Send messages to model</li>
     *   <li>If model calls tools → execute, append results, continue</li>
     *   <li>If model returns final text → done</li>
     * </ol>
     * Max iterations prevent infinite loops.
     */
    public AgentResult runReActAgent(String task, int maxIterations) {
        log.info("[AGENT-REACT] Task: {}, maxIterations: {}", task, maxIterations);

        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(task));
        List<String> steps = new ArrayList<>();

        for (int i = 0; i < maxIterations; i++) {
            log.info("[AGENT-REACT] Iteration {}", i + 1);

            ChatResponse response = chatModel.call(
                    new Prompt(messages,
                            ToolCallingChatOptions.builder()
                                    .toolCallbacks(
                                            org.springframework.ai.support.ToolCallbacks.from(
                                                    weatherTools, dateTimeTools, calculatorTools))
                                    .build())
            );

            AssistantMessage assistantMessage = response.getResult().getOutput();
            messages.add(assistantMessage);

            // No tool calls → final answer reached
            if (assistantMessage.getToolCalls() == null || assistantMessage.getToolCalls().isEmpty()) {
                log.info("[AGENT-REACT] Final answer reached after {} iteration(s)", i + 1);
                return new AgentResult(assistantMessage.getText(), steps, i + 1);
            }

            // Execute tool calls and append results
            assistantMessage.getToolCalls().forEach(toolCall -> {
                String step = "Tool: %s(%s)".formatted(toolCall.name(), toolCall.arguments());
                steps.add(step);
                log.info("[AGENT-REACT] {}", step);
            });
        }

        return new AgentResult("Max iterations reached without a final answer.", steps, maxIterations);
    }

    /** Holds the result of a ReAct agent run, including intermediate reasoning steps. */
    public record AgentResult(String answer, List<String> steps, int iterations) {}
}
