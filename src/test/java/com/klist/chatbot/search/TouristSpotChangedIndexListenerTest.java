package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.klist.chatbot.domain.touristspot.service.event.TouristSpotChangedEvent;
import com.klist.chatbot.infrastructure.search.sync.TouristSpotChangedIndexListener;
import com.klist.chatbot.infrastructure.search.sync.TouristSpotIndexSyncService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

class TouristSpotChangedIndexListenerTest {

    @Test
    void synchronizesChangedTouristSpot() {
        TouristSpotIndexSyncService syncService = mock(TouristSpotIndexSyncService.class);
        TouristSpotChangedIndexListener listener = new TouristSpotChangedIndexListener(syncService);

        listener.onChanged(new TouristSpotChangedEvent(101L));

        verify(syncService).synchronize(101L);
    }

    @Test
    void listensOnlyAfterDatabaseCommit() throws NoSuchMethodException {
        Method method = TouristSpotChangedIndexListener.class
                .getMethod("onChanged", TouristSpotChangedEvent.class);

        TransactionalEventListener annotation = method.getAnnotation(TransactionalEventListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
        assertThat(annotation.fallbackExecution()).isFalse();
    }
}
