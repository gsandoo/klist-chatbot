package com.klist.chatbot.domain.region.repository;

import com.klist.chatbot.domain.region.domain.entity.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RegionJpaRepository extends JpaRepository<Region, Long> {

    @Query("""
            select r
            from Region r
            where r.areaCode = :areaCode
              and (
                    (:sigunguCode is null and r.sigunguCode is null)
                    or r.sigunguCode = :sigunguCode
              )
            """)
    Optional<Region> findByAreaCodeAndNullableSigunguCode(
            @Param("areaCode") String areaCode,
            @Param("sigunguCode") String sigunguCode
    );
}
