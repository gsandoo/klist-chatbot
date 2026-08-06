package com.klist.chatbot.domain.touristspot.service.result;

import java.util.List;

public record TouristSpotImportSummary(
        int total,
        int created,
        int updated,
        int skipped,
        int failed,
        List<TouristSpotImportResult> results
) {

    public static TouristSpotImportSummary from(List<TouristSpotImportResult> results) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        for (TouristSpotImportResult result : results) {
            if (result.isCreated()) {
                created++;
            } else if (result.isUpdated()) {
                updated++;
            } else if (result.isSkipped()) {
                skipped++;
            } else if (result.isFailed()) {
                failed++;
            }
        }
        return new TouristSpotImportSummary(results.size(), created, updated, skipped, failed, List.copyOf(results));
    }
}
