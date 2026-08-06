package com.klist.chatbot.infrastructure.tourapi.ingestion;

import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportStatus;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportSummary;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClient;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClientResult;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiPage;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectRequest;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrationResult;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrationStatus;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrator;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TourApiIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TourApiIngestionService.class);

    private final TourApiClient tourApiClient;
    private final TourApiImportOrchestrator orchestrator;

    public TourApiIngestionService(TourApiClient tourApiClient, TourApiImportOrchestrator orchestrator) {
        this.tourApiClient = tourApiClient;
        this.orchestrator = orchestrator;
    }

    public TourApiIngestionSummary ingestAll(int numOfRows) {
        return ingestAll(numOfRows, 0, 0);
    }

    public TourApiIngestionSummary ingestAll(int numOfRows, int maxPages, int maxItems) {
        if (numOfRows < 1) {
            throw new IllegalArgumentException("numOfRows must be positive");
        }
        if (maxPages < 0 || maxItems < 0) {
            throw new IllegalArgumentException("maxPages and maxItems must not be negative");
        }

        List<TouristSpotImportResult> results = new ArrayList<>();
        Instant startedAt = Instant.now();
        int pageNo = 1;
        int requestedPages = 0;
        int pagesProcessed = 0;
        int sourceItems = 0;
        String pageFailureReason = null;
        log.info("TourAPI ingestion started. numOfRows={}, maxPages={}, maxItems={}",
                numOfRows, maxPages, maxItems);

        while (true) {
            if (maxPages > 0 && pagesProcessed >= maxPages) {
                break;
            }
            requestedPages++;
            TourApiClientResult<TourApiPage<TourApiAreaBasedListItem>> pageResult =
                    tourApiClient.getAreaBasedListPage(pageNo, numOfRows);
            if (pageResult == null || !pageResult.isSuccess()) {
                String reason = pageResult == null ? "Client returned no page result." : pageResult.reason();
                pageFailureReason = reason;
                log.error("TourAPI page collection failed. pageNo={}, reason={}", pageNo, reason);
                results.add(failed(null, "Page " + pageNo + " collection failed: " + reason));
                break;
            }

            TourApiPage<TourApiAreaBasedListItem> page = pageResult.value();
            pagesProcessed++;
            sourceItems += page.items().size();
            log.info("TourAPI page collected. pageNo={}, items={}, totalCount={}",
                    page.pageNo(), page.items().size(), page.totalCount());
            for (TourApiAreaBasedListItem item : page.items()) {
                if (maxItems > 0 && results.size() >= maxItems) {
                    break;
                }
                results.add(importItem(item));
            }

            boolean itemLimitReached = maxItems > 0 && results.size() >= maxItems;
            if (itemLimitReached || !page.hasNext() || page.items().isEmpty()) {
                break;
            }
            pageNo++;
        }

        TouristSpotImportSummary summary = TouristSpotImportSummary.from(results);
        List<Long> failedContentIds = results.stream()
                .filter(TouristSpotImportResult::isFailed)
                .map(TouristSpotImportResult::tourApiContentId)
                .filter(java.util.Objects::nonNull)
                .toList();
        TourApiIngestionSummary ingestionSummary = new TourApiIngestionSummary(
                requestedPages,
                pagesProcessed,
                sourceItems,
                summary,
                failedContentIds,
                pageFailureReason,
                startedAt,
                Instant.now()
        );
        log.info("TourAPI ingestion completed. total={}, created={}, updated={}, skipped={}, failed={}",
                summary.total(), summary.created(), summary.updated(), summary.skipped(), summary.failed());
        return ingestionSummary;
    }

    private TouristSpotImportResult importItem(TourApiAreaBasedListItem item) {
        String contentId = item == null ? null : item.contentid();
        try {
            TourApiImportOrchestrationResult result =
                    orchestrator.importOne(new TourApiCollectRequest(item));
            if (result.status() == TourApiImportOrchestrationStatus.COLLECT_FAILED) {
                String reason = result.collectResult() == null
                        ? "Collector returned no result."
                        : result.collectResult().warnings().toString();
                log.warn("TourAPI item collection failed. contentId={}, reason={}", contentId, reason);
                return failed(toLong(contentId), "TourAPI item collection failed: " + reason);
            }
            TouristSpotImportResult importResult = result.importResult();
            if (importResult == null) {
                return failed(toLong(contentId), "Import result is missing.");
            }
            return importResult;
        } catch (RuntimeException exception) {
            log.error("TourAPI item ingestion failed. contentId={}, error={}",
                    contentId, exception.getClass().getSimpleName());
            return failed(toLong(contentId), "Unexpected item ingestion failure.");
        }
    }

    private static TouristSpotImportResult failed(Long contentId, String message) {
        return TouristSpotImportResult.of(
                contentId, TouristSpotImportStatus.FAILED, message, null, List.of()
        );
    }

    private static Long toLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
