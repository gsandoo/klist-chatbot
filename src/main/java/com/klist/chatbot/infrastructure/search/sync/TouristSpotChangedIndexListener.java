package com.klist.chatbot.infrastructure.search.sync;

import com.klist.chatbot.domain.touristspot.service.event.TouristSpotChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TouristSpotChangedIndexListener {

    private final TouristSpotIndexSyncService syncService;

    public TouristSpotChangedIndexListener(TouristSpotIndexSyncService syncService) {
        this.syncService = syncService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChanged(TouristSpotChangedEvent event) {
        syncService.synchronize(event.touristSpotId());
    }
}
