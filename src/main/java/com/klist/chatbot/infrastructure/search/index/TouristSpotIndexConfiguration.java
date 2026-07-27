package com.klist.chatbot.infrastructure.search.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TouristSpotIndexProperties.class)
public class TouristSpotIndexConfiguration {

    @Bean
    TouristSpotIndexResourceLoader touristSpotIndexResourceLoader(
            TouristSpotIndexProperties properties
    ) {
        return new TouristSpotIndexResourceLoader(new ObjectMapper(), properties);
    }

    @Bean
    TouristSpotIndexManager touristSpotIndexManager(
            ElasticsearchOperations operations,
            TouristSpotIndexProperties properties,
            TouristSpotIndexResourceLoader resourceLoader
    ) {
        return new TouristSpotIndexManager(operations, properties, resourceLoader);
    }

    @Bean
    TouristSpotIndexingGateway touristSpotIndexingGateway(
            ElasticsearchOperations operations,
            TouristSpotIndexProperties properties
    ) {
        return new ElasticsearchTouristSpotIndexingGateway(operations, properties);
    }
}
