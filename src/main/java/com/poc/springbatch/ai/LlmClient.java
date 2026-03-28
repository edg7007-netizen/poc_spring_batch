package com.poc.springbatch.ai;

/**
 * Abstraction over a large language model (LLM) chat endpoint.
 *
 * By depending on this interface instead of a specific SDK, the application
 * can swap between AWS Bedrock, OpenAI, or a local stub without any changes
 * to the business logic.
 */
public interface LlmClient {

    /**
     * Sends a prompt and returns the model's text response.
     *
     * @param prompt the full prompt string to send to the model.
     * @return the model-generated text.
     */
    String chat(String prompt);
}
