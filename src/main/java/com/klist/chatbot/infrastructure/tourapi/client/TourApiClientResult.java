package com.klist.chatbot.infrastructure.tourapi.client;

import java.util.Objects;

public record TourApiClientResult<T>(
        TourApiClientStatus status,
        T value,
        String reason
) {

    public TourApiClientResult {
        Objects.requireNonNull(status, "status must not be null");
        if (status == TourApiClientStatus.SUCCESS) {
            Objects.requireNonNull(value, "successful result value must not be null");
        }
    }

    public static <T> TourApiClientResult<T> success(T value) {
        return new TourApiClientResult<>(TourApiClientStatus.SUCCESS, value, null);
    }

    public static <T> TourApiClientResult<T> empty(String reason) {
        return new TourApiClientResult<>(TourApiClientStatus.EMPTY, null, reason);
    }

    public static <T> TourApiClientResult<T> failure(String reason) {
        return new TourApiClientResult<>(TourApiClientStatus.FAILURE, null, reason);
    }

    public boolean isSuccess() {
        return status == TourApiClientStatus.SUCCESS;
    }
}
