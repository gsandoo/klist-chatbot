package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexProperties;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexResourceLoader;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class TouristSpotIndexResourceLoaderTest {

    private final TouristSpotIndexResourceLoader loader = new TouristSpotIndexResourceLoader(
            new ObjectMapper(),
            properties()
    );

    @Test
    void loadsKoreanAnalyzerSettings() {
        Map<String, Object> settings = loader.settings();

        Map<String, Object> analysis = map(settings.get("analysis"));
        Map<String, Object> tokenizer = map(map(analysis.get("tokenizer")).get("korean_nori_tokenizer"));
        Map<String, Object> analyzers = map(analysis.get("analyzer"));

        assertThat(tokenizer)
                .containsEntry("type", "nori_tokenizer")
                .containsEntry("decompound_mode", "mixed");
        assertThat(map(analyzers.get("korean_index")).get("tokenizer"))
                .isEqualTo("korean_nori_tokenizer");
        assertThat(map(analyzers.get("korean_search")).get("tokenizer"))
                .isEqualTo("korean_nori_tokenizer");
    }

    @Test
    void loadsSearchAndFilterMappings() {
        Map<String, Object> properties = map(loader.mappings().get("properties"));
        Map<String, Object> title = map(properties.get("title"));
        Map<String, Object> region = map(properties.get("region"));
        Map<String, Object> category = map(properties.get("category"));

        assertThat(title)
                .containsEntry("type", "text")
                .containsEntry("analyzer", "korean_index")
                .containsEntry("search_analyzer", "korean_search");
        assertThat(map(map(title.get("fields")).get("keyword")))
                .containsEntry("type", "keyword");
        assertThat(map(map(region.get("properties")).get("areaCode")))
                .containsEntry("type", "keyword");
        assertThat(map(map(category.get("properties")).get("largeCategoryCode")))
                .containsEntry("type", "keyword");
        assertThat(map(properties.get("coordinates")))
                .containsEntry("type", "geo_point");
    }

    private static TouristSpotIndexProperties properties() {
        TouristSpotIndexProperties properties = new TouristSpotIndexProperties();
        properties.setSettingsLocation(new ClassPathResource("elasticsearch/tourist-spots-settings.json"));
        properties.setMappingsLocation(new ClassPathResource("elasticsearch/tourist-spots-mappings.json"));
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
