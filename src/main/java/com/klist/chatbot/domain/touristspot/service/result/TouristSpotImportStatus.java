package com.klist.chatbot.domain.touristspot.service.result;

public enum TouristSpotImportStatus {
    CREATED,
    UPDATED,
    SKIPPED_NOT_MODIFIED,
    SKIPPED_OLDER_SOURCE,
    SKIPPED_MISSING_REQUIRED_FIELD,
    SKIPPED_MISSING_SOURCE_TIMESTAMP,
    SKIPPED_UNSUPPORTED_CONTENT_TYPE,
    FAILED
}
