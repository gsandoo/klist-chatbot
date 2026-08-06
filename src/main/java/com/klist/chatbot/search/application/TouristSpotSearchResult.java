package com.klist.chatbot.search.application;

import java.time.Duration;
import java.util.List;

public record TouristSpotSearchResult(
        List<TouristSpotSearchEvidence> evidence,
        long totalHits,
        Duration executionTime
) {

    public TouristSpotSearchResult {
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        if (totalHits < 0) {
            throw new IllegalArgumentException("totalHits must not be negative.");
        }
        executionTime = executionTime == null ? Duration.ZERO : executionTime;
    }

    public boolean isEmpty() {
        return evidence.isEmpty();
    }
}
