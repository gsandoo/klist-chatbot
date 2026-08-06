package com.klist.chatbot.infrastructure.tourapi.orchestration;

import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;

public record TourApiImportOrchestrationResult(
        TourApiImportOrchestrationStatus status,
        TourApiCollectResult collectResult,
        TourApiMappingResult<TouristSpotImportData> mappingResult,
        TouristSpotImportResult importResult
) {

    public static TourApiImportOrchestrationResult collectFailed(TourApiCollectResult collectResult) {
        return new TourApiImportOrchestrationResult(
                TourApiImportOrchestrationStatus.COLLECT_FAILED,
                collectResult,
                null,
                null
        );
    }

    public static TourApiImportOrchestrationResult importCompleted(
            TourApiCollectResult collectResult,
            TourApiMappingResult<TouristSpotImportData> mappingResult,
            TouristSpotImportResult importResult
    ) {
        return new TourApiImportOrchestrationResult(
                TourApiImportOrchestrationStatus.IMPORT_COMPLETED,
                collectResult,
                mappingResult,
                importResult
        );
    }
}
