package com.klist.chatbot.infrastructure.tourapi.ingestion;

import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportSummary;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record TourApiIngestionSummary(
        int requestedPages,
        int completedPages,
        int sourceItems,
        TouristSpotImportSummary importSummary,
        List<Long> failedContentIds,
        String pageFailureReason,
        Instant startedAt,
        Instant finishedAt
) {

    public TourApiIngestionSummary {
        failedContentIds = failedContentIds == null ? List.of() : List.copyOf(failedContentIds);
    }

    public Duration duration() {
        return Duration.between(startedAt, finishedAt);
    }

    public int total() {
        return importSummary.total();
    }

    public int created() {
        return importSummary.created();
    }

    public int updated() {
        return importSummary.updated();
    }

    public int skipped() {
        return importSummary.skipped();
    }

    public int failed() {
        return importSummary.failed();
    }
}
