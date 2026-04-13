package com.glow.service.tools;

import com.glow.tools.CalculatorTools;
import com.glow.tools.DateTimeTools;
import com.glow.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Module 6 — Function Calling / Tool Use.
 * <p>
 * Demonstrates how the LLM autonomously decides which tools to call,
 * executes them, and incorporates the results into its response.
 * Spring AI handles the entire tool-calling loop automatically.
 */
@Service
public class FunctionCallingService {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;

    public FunctionCallingService(ChatClient chatClient,
                                  WeatherTools weatherTools,
                                  DateTimeTools dateTimeTools,
                                  CalculatorTools calculatorTools) {
        this.chatClient = chatClient;
        this.weatherTools = weatherTools;
        this.dateTimeTools = dateTimeTools;
        this.calculatorTools = calculatorTools;
    }

    /**
     * Weather query — LLM calls WeatherTools.getCurrentWeather() automatically.
     */
    public String askWeather(String question) {
        return chatClient.prompt()
                .tools(weatherTools)
                .user(question)
                .call()
                .content();
    }

    /**
     * Date/time query — LLM calls DateTimeTools methods as needed.
     */
    public String askDateTime(String question) {
        return chatClient.prompt()
                .tools(dateTimeTools)
                .user(question)
                .call()
                .content();
    }

    /**
     * Math calculation — LLM delegates precise arithmetic to CalculatorTools.
     */
    public String calculate(String expression) {
        return chatClient.prompt()
                .tools(calculatorTools)
                .user("Calculate: " + expression)
                .call()
                .content();
    }

    /**
     * Multi-tool query — LLM can call any combination of tools in a single turn.
     * Spring AI handles sequential tool calls if multiple tools are needed.
     */
    public String askAll(String question) {
        return chatClient.prompt()
                .tools(weatherTools, dateTimeTools, calculatorTools)
                .user(question)
                .call()
                .content();
    }
}
