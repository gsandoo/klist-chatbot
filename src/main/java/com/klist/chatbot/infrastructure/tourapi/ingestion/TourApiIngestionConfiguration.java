package com.klist.chatbot.infrastructure.tourapi.ingestion;

import com.klist.chatbot.domain.touristspot.service.TouristSpotImporter;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClient;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollector;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiTouristSpotNormalizer;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TourApiIngestionProperties.class)
public class TourApiIngestionConfiguration {

    @Bean
    TourApiCollector tourApiCollector(TourApiClient tourApiClient,
            com.klist.chatbot.infrastructure.tourapi.client.TourApiProperties properties) {
        return new TourApiCollector(tourApiClient, properties.getLanguage());
    }

    @Bean
    TourApiTouristSpotNormalizer tourApiTouristSpotNormalizer(com.klist.chatbot.infrastructure.tourapi.client.TourApiProperties properties) {
        return new TourApiTouristSpotNormalizer(properties.getLanguage());
    }

    @Bean
    TourApiImportOrchestrator tourApiImportOrchestrator(
            TourApiCollector collector,
            TourApiTouristSpotNormalizer normalizer,
            TouristSpotImporter importer
    ) {
        return new TourApiImportOrchestrator(collector, normalizer, importer);
    }

    @Bean
    TourApiIngestionService tourApiIngestionService(
            TourApiClient tourApiClient,
            TourApiImportOrchestrator orchestrator
    ) {
        return new TourApiIngestionService(tourApiClient, orchestrator);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tour-api.ingestion", name = "enabled", havingValue = "true")
    TourApiDevIngestionRunner tourApiDevIngestionRunner(
            TourApiIngestionService ingestionService,
            TourApiIngestionProperties properties
    ) {
        return new TourApiDevIngestionRunner(ingestionService, properties);
    }
}
