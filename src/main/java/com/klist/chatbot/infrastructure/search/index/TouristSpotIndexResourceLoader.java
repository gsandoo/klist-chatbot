package com.klist.chatbot.infrastructure.search.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.springframework.data.elasticsearch.core.document.Document;

public class TouristSpotIndexResourceLoader {

    private final ObjectMapper objectMapper;
    private final TouristSpotIndexProperties properties;

    public TouristSpotIndexResourceLoader(
            ObjectMapper objectMapper,
            TouristSpotIndexProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public Map<String, Object> settings() {
        properties.validate();
        try (var inputStream = properties.getSettingsLocation().getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new TouristSpotIndexingException(
                    TouristSpotIndexOperation.CREATE_INDEX,
                    "Unable to read tourist-spots index settings.",
                    exception
            );
        }
    }

    public Document mappings() {
        properties.validate();
        try (var inputStream = properties.getMappingsLocation().getInputStream()) {
            return Document.parse(new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            throw new TouristSpotIndexingException(
                    TouristSpotIndexOperation.CREATE_INDEX,
                    "Unable to read tourist-spots index mappings.",
                    exception
            );
        }
    }
}
