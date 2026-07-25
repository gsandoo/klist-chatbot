package com.klist.chatbot.domain.touristspot.repository;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface TouristSpotJpaRepository extends JpaRepository<TouristSpot, Long> {

    Optional<TouristSpot> findByTourApiContentId(Long tourApiContentId);
}
