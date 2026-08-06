package com.klist.chatbot.chat.application.llm;

import java.util.Objects;

public class LlmClientException extends RuntimeException {

    private final LlmFailureType failureType;
    private final Integer httpStatus;
    private final String providerCode;
    private final boolean retryable;

    public LlmClientException(
            LlmFailureType failureType,
            String message,
            Integer httpStatus,
            String providerCode,
            boolean retryable,
            Throwable cause
    ) {
        super(message, cause);
        this.failureType = Objects.requireNonNull(failureType, "failureType must not be null");
        this.httpStatus = httpStatus;
        this.providerCode = providerCode;
        this.retryable = retryable;
    }

    public LlmFailureType failureType() {
        return failureType;
    }

    public Integer httpStatus() {
        return httpStatus;
    }

    public String providerCode() {
        return providerCode;
    }

    public boolean retryable() {
        return retryable;
    }
}
