package com.klist.chatbot.global.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> errors
) {

    public ErrorResponse {
        errors = List.copyOf(errors);
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public static ErrorResponse of(String code, String message, List<FieldError> errors) {
        return new ErrorResponse(code, message, errors);
    }

    public record FieldError(
            String field,
            Object value,
            String reason
    ) {
    }
}
