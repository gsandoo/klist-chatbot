package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexInitializationResult;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexManager;
import com.klist.chatbot.search.application.TouristSpotFullReindexService;
import com.klist.chatbot.search.application.TouristSpotIndexBootstrapMode;
import com.klist.chatbot.search.application.TouristSpotIndexBootstrapProperties;
import com.klist.chatbot.search.application.TouristSpotIndexBootstrapRunner;
import com.klist.chatbot.search.application.TouristSpotReindexStatus;
import com.klist.chatbot.search.application.TouristSpotReindexSummary;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class TouristSpotIndexBootstrapRunnerTest {

    private final TouristSpotIndexManager indexManager = mock(TouristSpotIndexManager.class);
    private final TouristSpotFullReindexService reindexService = mock(TouristSpotFullReindexService.class);
    private final TouristSpotIndexBootstrapProperties properties = new TouristSpotIndexBootstrapProperties();
    private final TouristSpotIndexBootstrapRunner runner =
            new TouristSpotIndexBootstrapRunner(indexManager, reindexService, properties);

    @Test
    void doesNothingByDefault() {
        runner.run(new DefaultApplicationArguments());

        verify(indexManager, never()).initialize();
        verify(reindexService, never()).reindexAll();
    }

    @Test
    void initializesVersionedIndexAndAlias() {
        properties.setMode(TouristSpotIndexBootstrapMode.INITIALIZE);
        when(indexManager.initialize()).thenReturn(new TouristSpotIndexInitializationResult(
                "tourist-spots-v1", "tourist-spots", true, true
        ));

        runner.run(new DefaultApplicationArguments());

        verify(indexManager).initialize();
        verify(reindexService, never()).reindexAll();
    }

    @Test
    void reindexesPostgresqlSourceAndPublishesAlias() {
        properties.setMode(TouristSpotIndexBootstrapMode.REINDEX);
        when(reindexService.reindexAll()).thenReturn(summary(
                TouristSpotReindexStatus.SUCCESS, 100, 0, true
        ));

        runner.run(new DefaultApplicationArguments());

        verify(reindexService).reindexAll();
        verify(indexManager, never()).initialize();
    }

    @Test
    void failsApplicationStartupWhenReindexIsIncomplete() {
        properties.setMode(TouristSpotIndexBootstrapMode.REINDEX);
        when(reindexService.reindexAll()).thenReturn(summary(
                TouristSpotReindexStatus.FAILED, 90, 10, false
        ));

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bootstrap reindex failed")
                .hasMessageContaining("failureCount=10");
    }

    private static TouristSpotReindexSummary summary(
            TouristSpotReindexStatus status,
            long successCount,
            long failureCount,
            boolean aliasSwitched
    ) {
        return new TouristSpotReindexSummary(
                status,
                "tourist-spots-v2",
                successCount,
                failureCount,
                failureCount == 0 ? List.of() : List.of(101L),
                Duration.ofSeconds(2),
                aliasSwitched
        );
    }
}
