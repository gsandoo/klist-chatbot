package com.klist.chatbot.domain.touristspot.service.result;

import java.util.List;

public record TouristSpotImportResult(
        Long tourApiContentId,
        TouristSpotImportStatus status,
        String message,
        Long entityId,
        List<String> warnings
) {

    public static TouristSpotImportResult of(
            Long tourApiContentId,
            TouristSpotImportStatus status,
            String message,
            Long entityId,
            List<String> warnings
    ) {
        return new TouristSpotImportResult(tourApiContentId, status, message, entityId, List.copyOf(warnings));
    }

    public boolean isCreated() {
        return status == TouristSpotImportStatus.CREATED;
    }

    public boolean isUpdated() {
        return status == TouristSpotImportStatus.UPDATED;
    }

    public boolean isSkipped() {
        return status.name().startsWith("SKIPPED_");
    }

    public boolean isFailed() {
        return status == TouristSpotImportStatus.FAILED;
    }
}
