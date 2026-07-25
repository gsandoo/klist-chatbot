package com.klist.chatbot.infrastructure.tourapi.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class TourApiDevIngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TourApiDevIngestionRunner.class);

    private final TourApiIngestionService ingestionService;
    private final TourApiIngestionProperties properties;

    public TourApiDevIngestionRunner(
            TourApiIngestionService ingestionService,
            TourApiIngestionProperties properties
    ) {
        this.ingestionService = ingestionService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        int pageSize = properties.getPageSize();
        if (pageSize < 1) {
            throw new IllegalStateException("tour-api.ingestion.page-size must be positive");
        }

        log.info("One-time development TourAPI ingestion is starting. pageSize={}", pageSize);
        TourApiIngestionSummary summary = ingestionService.ingestAll(
                pageSize,
                properties.getMaxPages(),
                properties.getMaxItems()
        );
        log.info(
                "One-time development TourAPI ingestion finished. total={}, created={}, updated={}, skipped={}, failed={}",
                summary.total(),
                summary.created(),
                summary.updated(),
                summary.skipped(),
                summary.failed()
        );
    }
}
