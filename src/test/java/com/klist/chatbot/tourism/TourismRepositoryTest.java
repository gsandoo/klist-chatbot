package com.klist.chatbot.tourism;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klist.chatbot.domain.category.domain.entity.Category;
import com.klist.chatbot.domain.category.repository.CategoryRepository;
import com.klist.chatbot.domain.category.repository.CategoryRepositoryImpl;
import com.klist.chatbot.domain.region.domain.entity.Region;
import com.klist.chatbot.domain.region.repository.RegionRepository;
import com.klist.chatbot.domain.region.repository.RegionRepositoryImpl;
import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepository;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepositoryImpl;
import com.klist.chatbot.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        CategoryRepositoryImpl.class,
        RegionRepositoryImpl.class,
        TouristSpotRepositoryImpl.class
})
class TourismRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private TouristSpotRepository touristSpotRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsCategoryByContentTypeIdAndSmallCategoryCode() {
        Category category = categoryRepository.save(category(12, "A01010400"));
        entityManager.flush();
        entityManager.clear();

        assertThat(categoryRepository.findByContentTypeIdAndSmallCategoryCode(12, "A01010400"))
                .isPresent()
                .get()
                .extracting(Category::getLargeCategoryCode)
                .isEqualTo("A01");
    }

    @Test
    void findsRegionByAreaCodeAndSigunguCode() {
        Region region = regionRepository.save(region("01", "005", "Gwanak-gu"));
        entityManager.flush();
        entityManager.clear();

        assertThat(regionRepository.findByAreaCodeAndSigunguCode("01", "005"))
                .isPresent()
                .get()
                .extracting(Region::getRegionCode)
                .isEqualTo("01:005");
    }

    @Test
    void findsRegionWhenSigunguCodeIsNull() {
        Region region = regionRepository.save(region("01", null, "Seoul"));
        entityManager.flush();
        entityManager.clear();

        assertThat(regionRepository.findByAreaCodeAndSigunguCode("01", null))
                .isPresent()
                .get()
                .extracting(Region::getRegionCode)
                .isEqualTo(region.getAreaCode());
    }

    @Test
    void findsTouristSpotByTourApiContentId() {
        TouristSpot touristSpot = touristSpotRepository.save(touristSpot(126480L, "Original"));
        entityManager.flush();
        entityManager.clear();

        assertThat(touristSpotRepository.findByTourApiContentId(126480L))
                .isPresent()
                .get()
                .extracting(TouristSpot::getName)
                .isEqualTo(touristSpot.getName());
    }

    @Test
    void enforcesCategoryUniqueConstraint() {
        categoryRepository.save(category(12, "A01010400"));

        assertThatThrownBy(() -> categoryRepository.save(category(12, "A01010400")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void enforcesRegionUniqueConstraintForDerivedRegionCode() {
        regionRepository.save(region("01", null, "Seoul"));

        assertThatThrownBy(() -> regionRepository.save(region("01", null, "Seoul Duplicate")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void enforcesTouristSpotUniqueConstraint() {
        touristSpotRepository.save(touristSpot(126480L, "First"));

        assertThatThrownBy(() -> touristSpotRepository.save(touristSpot(126480L, "Duplicate")))
                .isInstanceOf(RuntimeException.class);
    }

    private static Category category(Integer contentTypeId, String smallCategoryCode) {
        return Category.builder()
                .contentTypeId(contentTypeId)
                .largeCategoryCode("A01")
                .middleCategoryCode("A0101")
                .smallCategoryCode(smallCategoryCode)
                .categoryName(null)
                .build();
    }

    private static Region region(String areaCode, String sigunguCode, String regionName) {
        return Region.builder()
                .areaCode(areaCode)
                .sigunguCode(sigunguCode)
                .regionName(regionName)
                .parentRegionCode(sigunguCode == null ? null : areaCode)
                .build();
    }

    private static TouristSpot touristSpot(Long tourApiContentId, String name) {
        return TouristSpot.builder()
                .tourApiContentId(tourApiContentId)
                .contentTypeId(12)
                .name(name)
                .sourceModifiedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .lastSyncedAt(LocalDateTime.of(2026, 1, 1, 1, 0))
                .build();
    }
}
