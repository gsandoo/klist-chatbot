package com.klist.chatbot.domain.touristspot.repository;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TouristSpotRepositoryImpl implements TouristSpotRepository {

    private final TouristSpotJpaRepository touristSpotJpaRepository;

    @Override
    public Optional<TouristSpot> findById(Long id) {
        return touristSpotJpaRepository.findById(id);
    }

    @Override
    public Optional<TouristSpot> findByTourApiContentId(Long tourApiContentId) {
        return touristSpotJpaRepository.findByTourApiContentId(tourApiContentId);
    }

    @Override
    public TouristSpot save(TouristSpot touristSpot) {
        return touristSpotJpaRepository.save(touristSpot);
    }

    @Override
    public List<TouristSpot> findPageAfterId(long lastSeenId, int pageSize) {
        if (lastSeenId < 0) {
            throw new IllegalArgumentException("lastSeenId must not be negative.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive.");
        }
        return touristSpotJpaRepository.findByIdGreaterThanOrderByIdAsc(
                lastSeenId,
                PageRequest.of(0, pageSize)
        );
    }
}
