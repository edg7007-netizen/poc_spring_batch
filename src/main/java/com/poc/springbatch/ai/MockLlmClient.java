package com.poc.springbatch.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock LLM client for local development and testing.
 * Activated when {@code app.ai.provider=mock}.
 *
 * Returns a placeholder response so the application can run without AWS credentials.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock")
public class MockLlmClient implements LlmClient {

    @Override
    public String chat(String prompt) {
        log.info("[MockLlmClient] Returning stub response (no Bedrock credentials needed)");
        return """
                === MOCK CHEAT SHEET (AI provider: mock) ===

                1. Opening: "Hello, this is a friendly reminder about your account."
                2. Key Talking Points:
                   - Acknowledge the balance owed
                   - Ask if there have been any changes in their financial situation
                   - Offer flexible payment options
                3. Negotiation Strategy: Propose a manageable instalment plan.
                4. Likely Objections:
                   - "I can't afford it" → Offer a smaller first payment
                   - "Call me later" → Schedule a specific callback time
                5. Red Flags: Watch for repeated cancellations of agreements.
                6. Closing: Confirm the commitment in writing / via the app.
                """;
    }
}
