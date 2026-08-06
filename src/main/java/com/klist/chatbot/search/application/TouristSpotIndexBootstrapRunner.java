package com.klist.chatbot.search.application;

import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexInitializationResult;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class TouristSpotIndexBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TouristSpotIndexBootstrapRunner.class);

    private final TouristSpotIndexManager indexManager;
    private final TouristSpotFullReindexService reindexService;
    private final TouristSpotIndexBootstrapProperties properties;

    public TouristSpotIndexBootstrapRunner(
            TouristSpotIndexManager indexManager,
            TouristSpotFullReindexService reindexService,
            TouristSpotIndexBootstrapProperties properties
    ) {
        this.indexManager = indexManager;
        this.reindexService = reindexService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        properties.validate();
        switch (properties.getMode()) {
            case NONE -> log.info("Tourist spot index bootstrap is disabled.");
            case INITIALIZE -> initialize();
            case REINDEX -> reindex();
        }
    }

    private void initialize() {
        TouristSpotIndexInitializationResult result = indexManager.initialize();
        log.info(
                "Tourist spot index bootstrap initialized. indexName={}, alias={}, indexCreated={}, aliasUpdated={}",
                result.indexName(),
                result.alias(),
                result.indexCreated(),
                result.aliasUpdated()
        );
    }

    private void reindex() {
        TouristSpotReindexSummary summary = reindexService.reindexAll();
        if (summary.status() != TouristSpotReindexStatus.SUCCESS || !summary.aliasSwitched()) {
            throw new IllegalStateException(
                    "Tourist spot bootstrap reindex failed. indexName=" + summary.indexName()
                            + ", failureCount=" + summary.failureCount()
            );
        }
        log.info(
                "Tourist spot index bootstrap reindexed. indexName={}, successCount={}, executionTime={}",
                summary.indexName(),
                summary.successCount(),
                summary.executionTime()
        );
    }
}
