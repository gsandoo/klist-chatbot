package com.klist.chatbot.infrastructure.search.index;

public class TouristSpotIndexingException extends RuntimeException {

    private final TouristSpotIndexOperation operation;

    public TouristSpotIndexingException(
            TouristSpotIndexOperation operation,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.operation = operation;
    }

    public TouristSpotIndexOperation operation() {
        return operation;
    }
}
