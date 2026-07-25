package com.klist.chatbot.infrastructure.tourapi.ingestion;

import java.time.Instant;

public record TourApiIngestionExecution(
        Instant startedAt,
        Instant finishedAt,
        TourApiIngestionSummary summary,
        String failure
) {

    public boolean isSuccess() {
        return summary != null && failure == null;
    }
}
