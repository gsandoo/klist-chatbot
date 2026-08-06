package com.klist.chatbot.infrastructure.search.sync;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepository;
import com.klist.chatbot.infrastructure.search.index.TouristSpotIndexingGateway;
import com.klist.chatbot.infrastructure.search.mapper.TouristSpotSearchDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TouristSpotIndexSyncService {

    private static final Logger log = LoggerFactory.getLogger(TouristSpotIndexSyncService.class);

    private final TouristSpotRepository repository;
    private final TouristSpotSearchDocumentMapper mapper;
    private final TouristSpotIndexingGateway indexingGateway;

    public TouristSpotIndexSyncService(
            TouristSpotRepository repository,
            TouristSpotSearchDocumentMapper mapper,
            TouristSpotIndexingGateway indexingGateway
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.indexingGateway = indexingGateway;
    }

    @Transactional(readOnly = true)
    public TouristSpotIndexSyncResult synchronize(Long touristSpotId) {
        validateId(touristSpotId);
        TouristSpot touristSpot = repository.findById(touristSpotId).orElse(null);
        if (touristSpot == null) {
            log.warn("Tourist spot index synchronization skipped because source was not found. touristSpotId={}",
                    touristSpotId);
            return TouristSpotIndexSyncResult.notFound(touristSpotId);
        }
        try {
            indexingGateway.save(mapper.map(touristSpot));
            return TouristSpotIndexSyncResult.indexed(touristSpotId);
        } catch (RuntimeException exception) {
            log.error("Tourist spot index synchronization failed. touristSpotId={}", touristSpotId, exception);
            return TouristSpotIndexSyncResult.failed(touristSpotId, exception);
        }
    }

    private void validateId(Long touristSpotId) {
        if (touristSpotId == null || touristSpotId <= 0) {
            throw new IllegalArgumentException("touristSpotId must be positive.");
        }
    }
}
