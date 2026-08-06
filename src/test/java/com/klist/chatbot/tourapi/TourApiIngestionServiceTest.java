package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.domain.touristspot.service.TouristSpotImporter;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportStatus;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClient;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClientResult;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiPage;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectRequest;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollector;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import com.klist.chatbot.infrastructure.tourapi.ingestion.TourApiIngestionService;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiTouristSpotNormalizer;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TourApiIngestionServiceTest {

    @Test
    void ingestsOnePage() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 2, 2, item("1"), item("2"));

        var summary = service(client).ingestAll(2);

        assertThat(client.requestedPages).containsExactly(1);
        assertThat(summary.total()).isEqualTo(2);
        assertThat(summary.created()).isEqualTo(2);
    }

    @Test
    void traversesMultiplePages() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 2, 5, item("1"), item("2"));
        client.page(2, 2, 5, item("3"), item("4"));
        client.page(3, 2, 5, item("5"));

        var summary = service(client).ingestAll(2);

        assertThat(client.requestedPages).containsExactly(1, 2, 3);
        assertThat(summary.total()).isEqualTo(5);
    }

    @Test
    void stopsAtLastPageWithoutRequestingAnotherPage() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 3, 4, item("1"), item("2"), item("3"));
        client.page(2, 3, 4, item("4"));

        service(client).ingestAll(3);

        assertThat(client.requestedPages).containsExactly(1, 2);
    }

    @Test
    void continuesAfterOneTouristSpotFails() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 3, 3, item("1"), item("2", "99"), item("3"));

        var summary = service(client).ingestAll(3);

        assertThat(summary.created()).isEqualTo(2);
        assertThat(summary.failed()).isEqualTo(1);
    }

    @Test
    void aggregatesCreatedSkippedAndFailedCounts() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 4, 4, item("1"), item("2"), item("3"), item("4", "99"));
        TouristSpotImporter importer = mapping -> {
            long contentId = mapping.value().tourApiContentId();
            TouristSpotImportStatus status = contentId == 2
                    ? TouristSpotImportStatus.SKIPPED_NOT_MODIFIED
                    : TouristSpotImportStatus.CREATED;
            return TouristSpotImportResult.of(contentId, status, "test", contentId, List.of());
        };

        var summary = service(client, importer).ingestAll(4);

        assertThat(summary.total()).isEqualTo(4);
        assertThat(summary.created()).isEqualTo(2);
        assertThat(summary.skipped()).isEqualTo(1);
        assertThat(summary.failed()).isEqualTo(1);
    }

    @Test
    void limitsPagesForSafeDevelopmentRun() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 2, 6, item("1"), item("2"));
        client.page(2, 2, 6, item("3"), item("4"));

        var summary = service(client).ingestAll(2, 1, 0);

        assertThat(client.requestedPages).containsExactly(1);
        assertThat(summary.total()).isEqualTo(2);
    }

    @Test
    void limitsItemsForSafeDevelopmentRun() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 5, 5, item("1"), item("2"), item("3"), item("4"), item("5"));

        var summary = service(client).ingestAll(5, 0, 2);

        assertThat(summary.total()).isEqualTo(2);
    }

    @Test
    void reportsIngestionMetricsAndFailedContentIds() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 2, 3, item("1"), item("2", "99"));
        client.page(2, 2, 3, item("3"));

        var summary = service(client).ingestAll(2);

        assertThat(summary.requestedPages()).isEqualTo(2);
        assertThat(summary.completedPages()).isEqualTo(2);
        assertThat(summary.sourceItems()).isEqualTo(3);
        assertThat(summary.failedContentIds()).containsExactly(2L);
        assertThat(summary.pageFailureReason()).isNull();
        assertThat(summary.duration()).isGreaterThanOrEqualTo(java.time.Duration.ZERO);
    }

    @Test
    void continuesWithNextItemWhenImporterThrows() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.page(1, 3, 3, item("1"), item("2"), item("3"));
        List<Long> importedIds = new ArrayList<>();
        TouristSpotImporter importer = mapping -> {
            long contentId = mapping.value().tourApiContentId();
            importedIds.add(contentId);
            if (contentId == 2) {
                throw new IllegalStateException("simulated database failure");
            }
            return TouristSpotImportResult.of(
                    contentId, TouristSpotImportStatus.CREATED, "created", contentId, List.of()
            );
        };

        var summary = service(client, importer).ingestAll(3);

        assertThat(importedIds).containsExactly(1L, 2L, 3L);
        assertThat(summary.created()).isEqualTo(2);
        assertThat(summary.failed()).isEqualTo(1);
        assertThat(summary.failedContentIds()).containsExactly(2L);
    }

    private static TourApiIngestionService service(FakeTourApiClient client) {
        TouristSpotImporter importer = mapping -> TouristSpotImportResult.of(
                mapping.value().tourApiContentId(),
                TouristSpotImportStatus.CREATED,
                "created",
                mapping.value().tourApiContentId(),
                List.of()
        );
        return service(client, importer);
    }

    private static TourApiIngestionService service(
            FakeTourApiClient client,
            TouristSpotImporter importer
    ) {
        TourApiImportOrchestrator orchestrator = new TourApiImportOrchestrator(
                new TourApiCollector(client),
                new TourApiTouristSpotNormalizer(),
                importer
        );
        return new TourApiIngestionService(client, orchestrator);
    }

    private static TourApiAreaBasedListItem item(String contentId) {
        return item(contentId, "12");
    }

    private static TourApiAreaBasedListItem item(String contentId, String contentTypeId) {
        return new TourApiAreaBasedListItem(
                contentId, contentTypeId, "Spot " + contentId,
                "address", null, "00000", "1", "1",
                "A", "B", "C", null, null, null, null, null,
                "126.9", "37.5", null, null, null,
                "20260101000000", "20260102000000"
        );
    }

    private static final class FakeTourApiClient implements TourApiClient {

        private final Map<Integer, TourApiPage<TourApiAreaBasedListItem>> pages = new HashMap<>();
        private final List<Integer> requestedPages = new ArrayList<>();

        void page(
                int pageNo,
                int numOfRows,
                int totalCount,
                TourApiAreaBasedListItem... items
        ) {
            pages.put(pageNo, new TourApiPage<>(List.of(items), pageNo, numOfRows, totalCount));
        }

        @Override
        public TourApiClientResult<TourApiPage<TourApiAreaBasedListItem>> getAreaBasedListPage(
                int pageNo,
                int numOfRows
        ) {
            requestedPages.add(pageNo);
            TourApiPage<TourApiAreaBasedListItem> page = pages.get(pageNo);
            return page == null
                    ? TourApiClientResult.empty("no page")
                    : TourApiClientResult.success(page);
        }

        @Override
        public TourApiClientResult<TourApiAreaBasedListItem> getAreaBasedList(TourApiCollectRequest request) {
            return pages.values().stream()
                    .flatMap(page -> page.items().stream())
                    .filter(item -> item.contentid().equals(request.contentId()))
                    .findFirst()
                    .map(TourApiClientResult::success)
                    .orElseGet(() -> TourApiClientResult.empty("not found"));
        }

        @Override
        public TourApiClientResult<TourApiDetailCommonItem> getDetailCommon(String contentId) {
            return TourApiClientResult.empty("optional");
        }

        @Override
        public TourApiClientResult<TourApiDetailIntroItem> getDetailIntro(
                String contentId,
                String contentTypeId
        ) {
            return TourApiClientResult.empty("optional");
        }

        @Override
        public TourApiClientResult<TourApiDetailImageItem> getDetailImage(String contentId) {
            return TourApiClientResult.empty("optional");
        }

        @Override
        public TourApiClientResult<TourApiCodeItem> getRegionCode(String areaCode, String sigunguCode) {
            return TourApiClientResult.empty("optional");
        }
    }
}
