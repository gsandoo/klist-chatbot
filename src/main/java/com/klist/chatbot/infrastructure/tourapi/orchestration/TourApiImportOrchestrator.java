package com.klist.chatbot.infrastructure.tourapi.orchestration;

import com.klist.chatbot.domain.touristspot.service.TouristSpotImporter;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectRequest;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectResult;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollector;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiTouristSpotNormalizer;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;

public class TourApiImportOrchestrator {

    private final TourApiCollector collector;
    private final TourApiTouristSpotNormalizer normalizer;
    private final TouristSpotImporter touristSpotImporter;

    public TourApiImportOrchestrator(
            TourApiCollector collector,
            TourApiTouristSpotNormalizer normalizer,
            TouristSpotImporter touristSpotImporter
    ) {
        this.collector = collector;
        this.normalizer = normalizer;
        this.touristSpotImporter = touristSpotImporter;
    }

    public TourApiImportOrchestrationResult importOne(TourApiCollectRequest request) {
        TourApiCollectResult collectResult = collector.collect(request);
        if (!collectResult.isCollectable()) {
            return TourApiImportOrchestrationResult.collectFailed(collectResult);
        }

        TourApiMappingResult<TouristSpotImportData> mappingResult = normalizer.normalize(
                collectResult.areaBasedListItem(),
                collectResult.detailCommonItem(),
                collectResult.detailIntroItem(),
                collectResult.detailImageItem(),
                collectResult.regionCodeItem()
        );
        TouristSpotImportResult importResult = touristSpotImporter.importOne(mappingResult);
        return TourApiImportOrchestrationResult.importCompleted(collectResult, mappingResult, importResult);
    }
}
