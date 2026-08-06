package com.klist.chatbot.chat.application.llm;

public record LlmGenerationResult(
        String outputText,
        String model,
        long inputTokens,
        long outputTokens
) {

    public LlmGenerationResult {
        outputText = requireText(outputText, "outputText must not be blank");
        model = requireText(model, "model must not be blank");
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("token counts must not be negative");
        }
    }

    public long totalTokens() {
        return inputTokens + outputTokens;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
