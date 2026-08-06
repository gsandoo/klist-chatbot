package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.domain.touristspot.service.TouristSpotImporter;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportStatus;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClient;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClientResult;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectRequest;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollector;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiTouristSpotNormalizer;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrationResult;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrationStatus;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrator;
import java.util.List;
import org.junit.jupiter.api.Test;

class TourApiImportOrchestratorTest {

    @Test
    void connectsCollectNormalizeAndImport() {
        RecordingImporter importer = new RecordingImporter();
        TourApiImportOrchestrator orchestrator = new TourApiImportOrchestrator(
                new TourApiCollector(new SuccessfulClient()),
                new TourApiTouristSpotNormalizer(),
                importer
        );

        TourApiImportOrchestrationResult result =
                orchestrator.importOne(new TourApiCollectRequest("126480"));

        assertThat(result.status()).isEqualTo(TourApiImportOrchestrationStatus.IMPORT_COMPLETED);
        assertThat(result.mappingResult().isSuccess()).isTrue();
        assertThat(result.importResult().status()).isEqualTo(TouristSpotImportStatus.CREATED);
        assertThat(importer.callCount).isEqualTo(1);
        assertThat(importer.lastMappingResult.value().tourApiContentId()).isEqualTo(126480L);
    }

    @Test
    void doesNotNormalizeOrImportWhenCollectionFails() {
        RecordingImporter importer = new RecordingImporter();
        TourApiClient client = new SuccessfulClient() {
            @Override
            public TourApiClientResult<TourApiAreaBasedListItem> getAreaBasedList(TourApiCollectRequest request) {
                return TourApiClientResult.failure("areaBasedList unavailable");
            }
        };
        TourApiImportOrchestrator orchestrator = new TourApiImportOrchestrator(
                new TourApiCollector(client),
                new TourApiTouristSpotNormalizer(),
                importer
        );

        TourApiImportOrchestrationResult result =
                orchestrator.importOne(new TourApiCollectRequest("126480"));

        assertThat(result.status()).isEqualTo(TourApiImportOrchestrationStatus.COLLECT_FAILED);
        assertThat(result.mappingResult()).isNull();
        assertThat(result.importResult()).isNull();
        assertThat(importer.callCount).isZero();
    }

    private static class RecordingImporter implements TouristSpotImporter {

        private int callCount;
        private TourApiMappingResult<TouristSpotImportData> lastMappingResult;

        @Override
        public TouristSpotImportResult importOne(
                TourApiMappingResult<TouristSpotImportData> mappingResult
        ) {
            callCount++;
            lastMappingResult = mappingResult;
            return TouristSpotImportResult.of(
                    mappingResult.value().tourApiContentId(),
                    TouristSpotImportStatus.CREATED,
                    "created",
                    1L,
                    List.of()
            );
        }
    }

    private static class SuccessfulClient implements TourApiClient {

        @Override
        public TourApiClientResult<TourApiAreaBasedListItem> getAreaBasedList(TourApiCollectRequest request) {
            return TourApiClientResult.success(new TourApiAreaBasedListItem(
                    "126480", "12", "Gwanaksan", "Seoul", null, "08826",
                    "01", "005", "A01", "A0101", "A01010400",
                    null, null, null, null, null, "126.9540987991", "37.4484036407", null,
                    null, null, "20040101000000", "20260101000000"
            ));
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
