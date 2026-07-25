package com.klist.chatbot.domain.touristspot.service;

import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;

public interface TouristSpotImporter {

    TouristSpotImportResult importOne(TourApiMappingResult<TouristSpotImportData> mappingResult);
}
