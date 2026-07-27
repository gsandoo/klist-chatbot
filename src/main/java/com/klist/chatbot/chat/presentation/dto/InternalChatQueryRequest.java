package com.klist.chatbot.chat.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalChatQueryRequest(
        @NotBlank
        @Size(max = 100)
        String sessionId,

        @NotBlank
        @Size(max = 100)
        String userId,

        @NotBlank
        @Size(max = 4000)
        String message,

        @Min(100)
        @Max(30000)
        Integer timeoutMs
) {

    private static final int DEFAULT_TIMEOUT_MS = 5000;

    public int effectiveTimeoutMs() {
        return timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }
}
