package com.klist.chatbot.chat.presentation.error;

import java.time.Instant;
import java.util.List;

public record InternalApiErrorResponse(
        String code,
        String message,
        String traceId,
        Instant timestamp,
        List<FieldViolation> errors
) {

    public InternalApiErrorResponse {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static InternalApiErrorResponse of(String code, String message, String traceId) {
        return new InternalApiErrorResponse(code, message, traceId, Instant.now(), List.of());
    }

    public record FieldViolation(
            String field,
            String reason
    ) {
    }
}
