package com.glow.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Module 6 — Weather Tool.
 * <p>
 * Demonstrates Spring AI {@link Tool} annotation for function calling.
 * In a real app these methods would call a weather API (e.g. OpenWeatherMap).
 * Here they return mock data to keep the demo self-contained.
 */
@Slf4j
@Component
public class WeatherTools {

    @Tool(description = "Get the current weather for a given city. Returns temperature (°C), humidity (%), and conditions.")
    public String getCurrentWeather(String city) {
        log.info("[TOOL] getCurrentWeather called with city={}", city);
        // Simulated response — replace with real API call
        return """
                City: %s
                Temperature: 22°C
                Humidity: 65%%
                Conditions: Partly cloudy
                Wind: 12 km/h NE
                """.formatted(city);
    }

    @Tool(description = "Get the weather forecast for the next N days for a given city.")
    public String getWeatherForecast(String city, int days) {
        log.info("[TOOL] getWeatherForecast called with city={}, days={}", city, days);
        return """
                Forecast for %s (next %d days):
                Day 1: Sunny, 25°C / 15°C
                Day 2: Cloudy, 20°C / 13°C
                Day 3: Rainy, 17°C / 11°C
                """.formatted(city, days);
    }
}
