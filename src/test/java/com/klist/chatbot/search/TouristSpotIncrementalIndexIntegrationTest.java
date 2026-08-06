package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepository;
import com.klist.chatbot.domain.touristspot.service.event.TouristSpotChangedEvent;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexingGateway;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class TouristSpotIncrementalIndexIntegrationTest {

    private static final AtomicLong CONTENT_ID = new AtomicLong(900_000L);

    @Autowired
    private TouristSpotRepository repository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private TouristSpotIndexingGateway indexingGateway;

    @Test
    void indexesOnlyAfterTransactionCommit() {
        Long[] savedId = new Long[1];

        transactionTemplate.executeWithoutResult(status -> {
            TouristSpot saved = repository.save(touristSpot());
            savedId[0] = saved.getId();
            eventPublisher.publishEvent(new TouristSpotChangedEvent(saved.getId()));

            verifyNoInteractions(indexingGateway);
        });

        verify(indexingGateway).save(org.mockito.ArgumentMatchers.argThat(document ->
                document.touristSpotId().equals(savedId[0]) && document.title().equals("증분 색인 관광지")));
    }

    @Test
    void doesNotIndexRolledBackTouristSpot() {
        clearInvocations(indexingGateway);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            TouristSpot saved = repository.save(touristSpot());
            eventPublisher.publishEvent(new TouristSpotChangedEvent(saved.getId()));
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(indexingGateway);
    }

    private static TouristSpot touristSpot() {
        return TouristSpot.builder()
                .tourApiContentId(CONTENT_ID.incrementAndGet())
                .contentTypeId(12)
                .name("증분 색인 관광지")
                .description("트랜잭션 커밋 후 색인 검증")
                .address("서울특별시 종로구")
                .sourceCreatedAt(LocalDateTime.of(2026, 8, 1, 0, 0))
                .sourceModifiedAt(LocalDateTime.of(2026, 8, 6, 0, 0))
                .lastSyncedAt(LocalDateTime.of(2026, 8, 6, 1, 0))
                .build();
    }
}
