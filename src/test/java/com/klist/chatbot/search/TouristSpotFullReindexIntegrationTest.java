package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepository;
import com.klist.chatbot.search.application.TouristSpotFullReindexService;
import com.klist.chatbot.search.application.TouristSpotReindexStatus;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("elasticsearch")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class TouristSpotFullReindexIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>("postgres:17-alpine");

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
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.elasticsearch.uris", ELASTICSEARCH::getHttpHostAddress);
        registry.add("search.tourist-spots.version", () -> "v2");
        registry.add("search.tourist-spots.reindex.page-size", () -> "1");
    }

    @Autowired
    private TouristSpotRepository repository;

    @Autowired
    private TouristSpotFullReindexService reindexService;

    @Autowired
    private ElasticsearchOperations operations;

    @Test
    void reindexesPostgresqlRowsIntoNewIndexAndPublishesAlias() {
        TouristSpot first = repository.save(entity(910001L, "경복궁"));
        TouristSpot second = repository.save(entity(910002L, "창덕궁"));

        var summary = reindexService.reindexAll();

        assertThat(summary.status()).isEqualTo(TouristSpotReindexStatus.SUCCESS);
        assertThat(summary.indexName()).isEqualTo("tourist-spots-v2");
        assertThat(summary.successCount()).isEqualTo(2);
        assertThat(summary.failureCount()).isZero();
        assertThat(summary.aliasSwitched()).isTrue();
        assertThat(operations.exists(first.getId().toString(), IndexCoordinates.of("tourist-spots-v2"))).isTrue();
        assertThat(operations.exists(second.getId().toString(), IndexCoordinates.of("tourist-spots-v2"))).isTrue();

        Map<String, ?> aliases = operations.indexOps(IndexCoordinates.of("tourist-spots-*"))
                .getAliases("tourist-spots");
        assertThat(aliases).containsKey("tourist-spots-v2");
    }

    private static TouristSpot entity(long contentId, String name) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 28, 10, 0);
        return TouristSpot.builder()
                .tourApiContentId(contentId)
                .contentTypeId(12)
                .name(name)
                .description("대표 관광지")
                .address("서울특별시 종로구")
                .sourceModifiedAt(now)
                .lastSyncedAt(now)
                .build();
    }
}
