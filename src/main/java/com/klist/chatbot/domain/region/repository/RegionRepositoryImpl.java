package com.klist.chatbot.domain.region.repository;

import com.klist.chatbot.domain.region.domain.entity.Region;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RegionRepositoryImpl implements RegionRepository {

    private final RegionJpaRepository regionJpaRepository;

    @Override
    public Optional<Region> findByAreaCodeAndSigunguCode(String areaCode, String sigunguCode) {
        return regionJpaRepository.findByAreaCodeAndNullableSigunguCode(areaCode, sigunguCode);
    }

    @Override
    public Region save(Region region) {
        return regionJpaRepository.save(region);
    }
}
