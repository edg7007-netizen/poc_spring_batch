package com.poc.springbatch.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;

/**
 * AWS Bedrock implementation of {@link LlmClient}.
 *
 * Uses the Anthropic Claude v2 model by default.
 * Activated when {@code app.ai.provider=bedrock} (the default).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "bedrock", matchIfMissing = true)
public class BedrockLlmClient implements LlmClient {

    private static final String HUMAN_TURN = "\n\nHuman: ";
    private static final String ASSISTANT_TURN = "\n\nAssistant:";

    private final BedrockRuntimeClient bedrockClient;
    private final String modelId;
    private final int maxTokens;
    private final double temperature;
    private final ObjectMapper objectMapper;

    public BedrockLlmClient(
            @Value("${app.ai.bedrock.region:us-east-1}") String region,
            @Value("${app.ai.bedrock.model-id:anthropic.claude-v2}") String modelId,
            @Value("${app.ai.bedrock.max-tokens:1024}") int maxTokens,
            @Value("${app.ai.bedrock.temperature:0.7}") double temperature) {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
        this.modelId = modelId;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String chat(String prompt) {
        log.debug("Calling Bedrock model={} maxTokens={}", modelId, maxTokens);
        try {
            String requestBody = buildAnthropicRequestBody(prompt);
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromByteArray(requestBody.getBytes(StandardCharsets.UTF_8)))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            return extractCompletion(responseBody);
        } catch (Exception e) {
            log.error("Bedrock invocation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Bedrock call failed: " + e.getMessage(), e);
        }
    }

    private String buildAnthropicRequestBody(String prompt) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("prompt", HUMAN_TURN + prompt + ASSISTANT_TURN);
        body.put("max_tokens_to_sample", maxTokens);
        body.put("temperature", temperature);
        body.putArray("stop_sequences").add(HUMAN_TURN.trim());
        return objectMapper.writeValueAsString(body);
    }

    private String extractCompletion(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody)
                .path("completion")
                .asText()
                .trim();
    }
}
