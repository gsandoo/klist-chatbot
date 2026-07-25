package com.klist.chatbot.domain.touristspot.repository;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TouristSpotRepositoryImpl implements TouristSpotRepository {

    private final TouristSpotJpaRepository touristSpotJpaRepository;

    @Override
    public Optional<TouristSpot> findByTourApiContentId(Long tourApiContentId) {
        return touristSpotJpaRepository.findByTourApiContentId(tourApiContentId);
    }

    @Override
    public TouristSpot save(TouristSpot touristSpot) {
        return touristSpotJpaRepository.save(touristSpot);
    }
}
