package com.klist.chatbot.domain.touristspot.repository;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.util.List;
import java.util.Optional;

public interface TouristSpotRepository {

    Optional<TouristSpot> findById(Long id);

    Optional<TouristSpot> findByTourApiContentId(Long tourApiContentId);

    TouristSpot save(TouristSpot touristSpot);

    List<TouristSpot> findPageAfterId(long lastSeenId, int pageSize);
}
