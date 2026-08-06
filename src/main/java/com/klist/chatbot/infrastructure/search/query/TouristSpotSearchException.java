package com.klist.chatbot.infrastructure.search.query;

public class TouristSpotSearchException extends RuntimeException {

    private final boolean retryable;

    public TouristSpotSearchException(String message, Throwable cause) {
        this(message, cause, false);
    }

    public TouristSpotSearchException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean retryable() {
        return retryable;
    }
}
