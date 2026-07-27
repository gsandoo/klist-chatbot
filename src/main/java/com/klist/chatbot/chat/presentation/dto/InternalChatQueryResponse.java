package com.klist.chatbot.chat.presentation.dto;

import java.util.List;
import java.util.Objects;

public record InternalChatQueryResponse(
        String answer,
        List<ChatSourceResponse> sources,
        String traceId,
        ChatQueryStatus status,
        long processingTimeMs
) {

    public InternalChatQueryResponse {
        Objects.requireNonNull(answer, "answer must not be null");
        sources = sources == null ? List.of() : List.copyOf(sources);
        Objects.requireNonNull(traceId, "traceId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (processingTimeMs < 0) {
            throw new IllegalArgumentException("processingTimeMs must not be negative");
        }
    }
}
