package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.infrastructure.search.document.TouristSpotSearchDocument;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexInitializationResult;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexManager;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexingGateway;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("elasticsearch")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class TouristSpotIndexingIntegrationTest {

    @Container
    static final ElasticsearchContainer ELASTICSEARCH =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.4.2")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node")
                    .withCommand(
                            "bash",
                            "-c",
                            "bin/elasticsearch-plugin install --batch analysis-nori"
                                    + " && /usr/local/bin/docker-entrypoint.sh eswrapper"
                    );

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", ELASTICSEARCH::getHttpHostAddress);
    }

    @Autowired
    private TouristSpotIndexManager indexManager;

    @Autowired
    private TouristSpotIndexingGateway gateway;

    @Autowired
    private ElasticsearchOperations operations;

    @BeforeEach
    void initializeIndex() {
        indexManager.initialize();
    }

    @Test
    void createsMappingAndUsesAliasForGatewayOperations() {
        TouristSpotIndexInitializationResult initialization = indexManager.initialize();

        assertThat(initialization.indexName()).isEqualTo("tourist-spots-v1");
        assertThat(operations.indexOps(IndexCoordinates.of("tourist-spots-v1")).exists()).isTrue();

        Map<String, Object> mapping = operations.indexOps(IndexCoordinates.of("tourist-spots-v1")).getMapping();
        Map<String, Object> fields = map(mapping.get("properties"));
        assertThat(map(fields.get("coordinates"))).containsEntry("type", "geo_point");
        assertThat(map(map(fields.get("title")).get("fields")))
                .containsKey("keyword");

        TouristSpotSearchDocument first = document(1001L, "경복궁");
        TouristSpotSearchDocument second = document(1002L, "창덕궁");
        gateway.save(first);
        gateway.saveAll(List.of(second));

        assertThat(gateway.exists(1001L)).isTrue();
        assertThat(gateway.exists(1002L)).isTrue();

        gateway.delete(1001L);
        assertThat(gateway.exists(1001L)).isFalse();
    }

    private static TouristSpotSearchDocument document(Long id, String title) {
        return new TouristSpotSearchDocument(
                id,
                title,
                "한국의 대표적인 궁궐",
                "서울특별시 종로구",
                null,
                null,
                new GeoPoint(37.5796, 126.9770),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
