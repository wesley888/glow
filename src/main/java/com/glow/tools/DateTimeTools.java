package com.glow.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Module 6 — Date & Time Tools.
 * Shows how to expose real JVM capabilities as LLM tools.
 */
@Slf4j
@Component
public class DateTimeTools {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Tool(description = "Get the current date and time for a given timezone (e.g. 'Asia/Shanghai', 'UTC', 'America/New_York').")
    public String getCurrentDateTime(String timezone) {
        log.info("[TOOL] getCurrentDateTime called with timezone={}", timezone);
        ZoneId zoneId = ZoneId.of(timezone);
        return ZonedDateTime.now(zoneId).format(FORMATTER);
    }

    @Tool(description = "Get the current date and time in the server's local timezone.")
    public String getLocalDateTime() {
        log.info("[TOOL] getLocalDateTime called");
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
