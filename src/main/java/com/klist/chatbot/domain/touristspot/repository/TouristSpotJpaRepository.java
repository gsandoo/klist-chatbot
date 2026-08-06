package com.klist.chatbot.domain.touristspot.repository;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

interface TouristSpotJpaRepository extends JpaRepository<TouristSpot, Long> {

    Optional<TouristSpot> findByTourApiContentId(Long tourApiContentId);

    List<TouristSpot> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}
