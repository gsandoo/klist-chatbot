package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.infrastructure.tourapi.ingestion.TourApiIngestionService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "tour-api.ingestion.enabled=false"
})
@ActiveProfiles("test")
@Import(TourApiLivePostgreSqlIntegrationTest.ContainerConfiguration.class)
@Tag("tourapi-live")
@Tag("postgresql")
@EnabledIfEnvironmentVariable(named = "TOUR_API_SERVICE_KEY", matches = ".+")
class TourApiLivePostgreSqlIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("tour-api.service-key", () -> System.getenv("TOUR_API_SERVICE_KEY"));
    }

    @Autowired
    private TourApiIngestionService ingestionService;

    @Test
    void ingestsRealTourApiItemsIntoPostgreSql() {
        var summary = ingestionService.ingestAll(10, 1, 10);

        assertThat(summary.completedPages()).isEqualTo(1);
        assertThat(summary.sourceItems()).isPositive();
        assertThat(summary.total()).isBetween(1, 10);
        assertThat(summary.created() + summary.updated() + summary.skipped()).isPositive();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ContainerConfiguration {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgreSqlContainer() {
            return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
        }
    }
}
