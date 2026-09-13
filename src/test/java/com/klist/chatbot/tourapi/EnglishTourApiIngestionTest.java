package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.*;
import com.klist.chatbot.domain.touristspot.service.*;
import com.klist.chatbot.domain.category.repository.CategoryRepositoryImpl;
import com.klist.chatbot.domain.category.service.CategoryPersistenceResolver;
import com.klist.chatbot.domain.region.repository.RegionRepositoryImpl;
import com.klist.chatbot.domain.region.service.RegionPersistenceResolver;
import com.klist.chatbot.global.config.JpaAuditingConfig;
import com.klist.chatbot.infrastructure.search.mapper.TouristSpotSearchDocumentMapper;
import com.klist.chatbot.infrastructure.tourapi.client.*;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollector;
import com.klist.chatbot.infrastructure.tourapi.ingestion.TourApiIngestionService;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiTouristSpotNormalizer;
import com.klist.chatbot.infrastructure.tourapi.orchestration.TourApiImportOrchestrator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DataJpaTest
@Import({JpaAuditingConfig.class, TouristSpotRepositoryImpl.class, TouristSpotImportService.class,
        CategoryRepositoryImpl.class, CategoryPersistenceResolver.class,
        RegionRepositoryImpl.class, RegionPersistenceResolver.class})
class EnglishTourApiIngestionTest {
    @Autowired TouristSpotRepository repository;
    @Autowired TouristSpotImportService importer;
    @MockitoBean TouristSpotChangePublisher publisher;

    @Test void collectsEnglishApiAndKeepsSameNumericKoreanIdSeparate() {
        LocalDateTime modified = LocalDateTime.of(2026, 4, 16, 15, 28, 35);
        TouristSpot korean = repository.save(TouristSpot.builder().tourApiContentId(264337L)
                .contentTypeId(12).name("Korean fixture").sourceModifiedAt(modified)
                .lastSyncedAt(modified).build());
        TourApiProperties properties = new TourApiProperties();
        properties.setLanguage("en");
        properties.setServiceKey("test-key");
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        String base = "https://apis.data.go.kr/B551011/EngService2/";
        // Selected real response fields observed on 2026-09-10. No legacy area/category codes.
        String item = """
                {"contentid":"264337","contenttypeid":"76","title":"Gyeongbokgung Palace",
                 "addr1":"161 Sajik-ro, Jongno-gu, Seoul","modifiedtime":"20260416152835",
                 "mapx":"126.97672186606306","mapy":"37.576030700049394",
                 "lDongRegnCd":"11","lDongSignguCd":"110"}
                """;
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(base + "areaBasedList2?")))
                .andRespond(withSuccess(response(item), MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(base + "detailCommon2?")))
                .andRespond(withSuccess(response(item.substring(0, item.lastIndexOf('}'))
                        + ",\"overview\":\"A royal palace built in 1395.\"}"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(base + "detailIntro2?")))
                .andRespond(withSuccess(response("{\"contentid\":\"264337\",\"contenttypeid\":\"76\",\"usetime\":\"09:00-18:00\"}"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(base + "detailImage2?")))
                .andRespond(withSuccess(response("{}"), MediaType.APPLICATION_JSON));
        TourApiClient client = new RestTourApiClient(builder.build(), new ObjectMapper(), properties);
        var orchestrator = new TourApiImportOrchestrator(new TourApiCollector(client, "en"),
                new TourApiTouristSpotNormalizer("en"), importer);
        new TourApiIngestionService(client, orchestrator).ingestAll(1, 1, 1);

        TouristSpot english = repository.findByTourApiContentIdAndLanguage(264337L, "en").orElseThrow();
        assertThat(english.getId()).isNotEqualTo(korean.getId());
        assertThat(english.getContentTypeId()).isEqualTo(76);
        assertThat(english.getDescription()).isEqualTo("A royal palace built in 1395.");
        assertThat(english.getOpeningHours()).isEqualTo("09:00-18:00");
        assertThat(repository.findByTourApiContentId(264337L).orElseThrow().getName()).isEqualTo("Korean fixture");
        assertThat(new TouristSpotSearchDocumentMapper().map(english).language()).isEqualTo("en");
        server.verify();
    }

    private String response(String item) {
        return "{\"response\":{\"header\":{\"resultCode\":\"0000\",\"resultMsg\":\"OK\"},"
                + "\"body\":{\"items\":{\"item\":[" + item + "]},\"pageNo\":1,\"numOfRows\":1,\"totalCount\":1}}}";
    }
}
