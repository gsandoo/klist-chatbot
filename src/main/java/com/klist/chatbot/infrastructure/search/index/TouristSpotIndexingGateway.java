package com.klist.chatbot.infrastructure.search.index;

import com.klist.chatbot.infrastructure.search.document.TouristSpotSearchDocument;
import java.util.List;

public interface TouristSpotIndexingGateway {

    TouristSpotSearchDocument save(TouristSpotSearchDocument document);

    List<TouristSpotSearchDocument> saveAll(List<TouristSpotSearchDocument> documents);

    void delete(Long touristSpotId);

    boolean exists(Long touristSpotId);
}
