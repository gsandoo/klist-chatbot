package com.klist.chatbot.chat.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record InternalAudioChatQueryRequest(
        @NotNull
        UUID requestId,

        @NotBlank
        @Size(max = 100)
        String sessionId,

        @NotBlank
        @Size(max = 100)
        String userId,

        @Valid
        @Size(max = 10)
        List<ChatContextMessage> context,

        @Min(100)
        @Max(30000)
        Integer timeoutMs,
        @jakarta.validation.constraints.Pattern(regexp = "ko|en") String language
) {
    public InternalAudioChatQueryRequest(
            UUID requestId,
            String sessionId,
            String userId,
            List<ChatContextMessage> context,
            Integer timeoutMs
    ) {
        this(
                requestId,
                sessionId,
                userId,
                context,
                timeoutMs,
                "ko"
        );
    }


    public InternalAudioChatQueryRequest {
        language = language == null ? "ko" : language;
        context = context == null ? List.of() : List.copyOf(context);
    }

    public InternalChatQueryRequest toChatRequest(String transcription) {
        return new InternalChatQueryRequest(
                requestId,
                sessionId,
                userId,
                transcription,
                context,
                timeoutMs, language
        );
    }
}
