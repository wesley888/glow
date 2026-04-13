package com.glow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // Use a fake key so the context loads without a real API key in CI
        "spring.ai.openai.api-key=sk-test-placeholder",
        // Disable auto-config that requires network access at startup
        "spring.ai.openai.chat.options.model=gpt-4o"
})
class GlowApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that all beans wire up correctly
    }
}
