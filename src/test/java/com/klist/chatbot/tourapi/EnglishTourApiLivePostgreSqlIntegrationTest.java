package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;
import com.klist.chatbot.infrastructure.tourapi.ingestion.TourApiIngestionService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {"tour-api.language=en", "tour-api.ingestion.enabled=false"})
@Import(TourApiLivePostgreSqlIntegrationTest.ContainerConfiguration.class)
@Tag("tourapi-live")
@Tag("postgresql")
@EnabledIfEnvironmentVariable(named = "TOUR_API_SERVICE_KEY", matches = ".+")
class EnglishTourApiLivePostgreSqlIntegrationTest {
    @Autowired TourApiIngestionService ingestion;
    @Autowired JdbcTemplate jdbc;

    @Test void ingestsThreeEnglishRecordsAndReimportsWithoutDuplicates() {
        var first = ingestion.ingestAll(3, 1, 3);
        assertThat(first.failed()).isZero();
        assertThat(first.created()).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tourist_spot WHERE language = 'en'", Long.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tourist_spot WHERE language = 'ko'", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tourist_spot WHERE description IS NOT NULL", Long.class)).isPositive();
        var second = ingestion.ingestAll(3, 1, 3);
        assertThat(second.failed()).isZero();
        assertThat(second.created()).isZero();
        assertThat(second.updated() + second.skipped()).isEqualTo(3);
    }
}
