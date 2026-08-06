package com.klist.chatbot.search.application;

import java.time.Duration;
import java.util.List;

public record TouristSpotReindexSummary(
        TouristSpotReindexStatus status,
        String indexName,
        long successCount,
        long failureCount,
        List<Long> failedTouristSpotIds,
        Duration executionTime,
        boolean aliasSwitched
) {
    public TouristSpotReindexSummary {
        failedTouristSpotIds = List.copyOf(failedTouristSpotIds);
    }
}
