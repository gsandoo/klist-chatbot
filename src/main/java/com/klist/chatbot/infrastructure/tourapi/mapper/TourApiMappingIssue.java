package com.klist.chatbot.infrastructure.tourapi.mapper;

public record TourApiMappingIssue(
        TourApiMappingIssueCode code,
        String fieldName,
        String message
) {
}
