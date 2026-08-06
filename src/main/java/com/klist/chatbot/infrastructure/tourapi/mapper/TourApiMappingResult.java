package com.klist.chatbot.infrastructure.tourapi.mapper;

import java.util.List;

public record TourApiMappingResult<T>(
        T value,
        List<TourApiMappingIssue> issues
) {

    public boolean isSuccess() {
        return value != null;
    }

    public static <T> TourApiMappingResult<T> success(T value, List<TourApiMappingIssue> issues) {
        return new TourApiMappingResult<>(value, List.copyOf(issues));
    }

    public static <T> TourApiMappingResult<T> failure(List<TourApiMappingIssue> issues) {
        return new TourApiMappingResult<>(null, List.copyOf(issues));
    }
}
