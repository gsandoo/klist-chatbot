package com.klist.chatbot.tourism;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.domain.category.domain.entity.Category;
import com.klist.chatbot.domain.category.repository.CategoryRepository;
import com.klist.chatbot.domain.category.repository.CategoryRepositoryImpl;
import com.klist.chatbot.domain.region.domain.entity.Region;
import com.klist.chatbot.domain.region.repository.RegionRepository;
import com.klist.chatbot.domain.region.repository.RegionRepositoryImpl;
import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepository;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepositoryImpl;
import com.klist.chatbot.domain.touristspot.service.TouristSpotImportService;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportStatus;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportSummary;
import com.klist.chatbot.global.config.JpaAuditingConfig;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingIssue;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingIssueCode;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        CategoryRepositoryImpl.class,
        RegionRepositoryImpl.class,
        TouristSpotRepositoryImpl.class,
        TouristSpotImportService.class
})
class TouristSpotImportServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private TouristSpotRepository touristSpotRepository;

    @Autowired
    private TouristSpotImportService touristSpotImportService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createsNewTouristSpot() {
        Category category = categoryRepository.save(category(12, "A01010400"));
        Region region = regionRepository.save(region("1", "5", "Gwanak-gu"));

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();
        entityManager.clear();

        TouristSpot saved = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.CREATED);
        assertThat(saved.getCategoryId()).isEqualTo(category.getId());
        assertThat(saved.getRegionId()).isEqualTo(region.getId());
    }

    @Test
    void skipsSameSourceModifiedAt() {
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L)));

        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.SKIPPED_NOT_MODIFIED);
    }

    @Test
    void updatesWhenIncomingSourceModifiedAtIsNewer() {
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Updated name", "Updated description")));
        entityManager.flush();
        entityManager.clear();

        TouristSpot updated = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.UPDATED);
        assertThat(updated.getName()).isEqualTo("Updated name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void skipsOlderIncomingData() {
        touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Current", "Current description")));
        entityManager.flush();

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 1, 1, 0, 0), "Older", "Older description")));

        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.SKIPPED_OLDER_SOURCE);
    }

    @Test
    void skipsMissingIncomingSourceModifiedAt() {
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L,
                null, "Missing timestamp", "Missing timestamp description")));

        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.SKIPPED_MISSING_SOURCE_TIMESTAMP);
    }

    @Test
    void serviceLogicAllowsUpdateWhenExistingSourceModifiedAtIsNull() {
        InMemoryTouristSpotRepository touristSpotRepository = new InMemoryTouristSpotRepository(TouristSpot.builder()
                .tourApiContentId(126480L)
                .contentTypeId(12)
                .name("Existing")
                .sourceModifiedAt(null)
                .lastSyncedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build());
        TouristSpotImportService service = new TouristSpotImportService(
                new NoopCategoryRepository(),
                new NoopRegionRepository(),
                touristSpotRepository
        );

        TouristSpotImportResult result = service.importOne(success(importData(126480L)));

        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.UPDATED);
        assertThat(touristSpotRepository.saved.getSourceModifiedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void keepsExistingOptionalFieldsWhenIncomingValuesAreNull() {
        touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 1, 1, 0, 0), "Original", "Original description")));
        entityManager.flush();

        touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Updated", null)));
        entityManager.flush();
        entityManager.clear();

        TouristSpot updated = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Original description");
        assertThat(updated.getLatitude()).isEqualByComparingTo("37.4484036407");
    }

    @Test
    void changesRegionWhenNewRegionIsResolved() {
        regionRepository.save(region("1", "5", "Gwanak-gu"));
        Region newRegion = regionRepository.save(region("1", "13", "Mapo-gu"));
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Updated", "Updated description",
                "A01010400", "1", "13", null)));
        entityManager.flush();
        entityManager.clear();

        TouristSpot updated = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(updated.getRegionId()).isEqualTo(newRegion.getId());
    }

    @Test
    void keepsExistingRegionWhenNewRegionCannotBeResolved() {
        Region existingRegion = regionRepository.save(region("1", "5", "Gwanak-gu"));
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Updated", "Updated description",
                "A01010400", "9", "99", null)));
        entityManager.flush();
        entityManager.clear();

        TouristSpot updated = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(updated.getRegionId()).isEqualTo(existingRegion.getId());
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("Region was not created"));
    }

    @Test
    void changesCategoryWhenNewCategoryIsResolved() {
        categoryRepository.save(category(12, "A01010400"));
        Category newCategory = categoryRepository.save(category(12, "A01010500"));
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Updated", "Updated description",
                "A01010500", "1", "5", null)));
        entityManager.flush();
        entityManager.clear();

        TouristSpot updated = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(updated.getCategoryId()).isEqualTo(newCategory.getId());
    }

    @Test
    void keepsExistingCategoryWhenNewCategoryCannotBeResolved() {
        Category existingCategory = categoryRepository.save(category(12, "A01010400"));
        touristSpotImportService.importOne(success(importData(126480L)));
        entityManager.flush();

        TouristSpotImportResult result = touristSpotImportService.importOne(success(importData(126480L,
                LocalDateTime.of(2026, 2, 1, 0, 0), "Updated", "Updated description",
                null, "1", "5", null)));
        entityManager.flush();
        entityManager.clear();

        TouristSpot updated = touristSpotRepository.findByTourApiContentId(126480L).orElseThrow();
        assertThat(updated.getCategoryId()).isEqualTo(existingCategory.getId());
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("Category code is missing"));
    }

    @Test
    void skipsMissingRequiredFields() {
        TouristSpotImportResult result = touristSpotImportService.importOne(success(new TouristSpotImportData(
                null, 12, "Name", "Description", null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                "A01", "A0101", "A01010400", null, null, null,
                "1", "5", null, null, null
        )));

        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.SKIPPED_MISSING_REQUIRED_FIELD);
    }

    @Test
    void mapsUnsupportedContentTypeFailureToSkipStatus() {
        TouristSpotImportResult result = touristSpotImportService.importOne(TourApiMappingResult.failure(List.of(
                new TourApiMappingIssue(TourApiMappingIssueCode.UNSUPPORTED_CONTENT_TYPE_ID,
                        "contenttypeid", "Unsupported")
        )));

        assertThat(result.status()).isEqualTo(TouristSpotImportStatus.SKIPPED_UNSUPPORTED_CONTENT_TYPE);
    }

    @Test
    void aggregatesImportResults() {
        TouristSpotImportSummary summary = touristSpotImportService.importAll(List.of(
                success(importData(126480L)),
                success(importData(126480L)),
                TourApiMappingResult.failure(List.of(
                        new TourApiMappingIssue(TourApiMappingIssueCode.UNSUPPORTED_CONTENT_TYPE_ID,
                                "contenttypeid", "Unsupported")
                ))
        ));

        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.created()).isEqualTo(1);
        assertThat(summary.skipped()).isEqualTo(2);
    }

    private static TourApiMappingResult<TouristSpotImportData> success(TouristSpotImportData data) {
        return TourApiMappingResult.success(data, List.of());
    }

    private static TouristSpotImportData importData(Long tourApiContentId) {
        return importData(tourApiContentId, LocalDateTime.of(2026, 1, 1, 0, 0), "Gwanaksan", "Description");
    }

    private static TouristSpotImportData importData(
            Long tourApiContentId,
            LocalDateTime sourceModifiedAt,
            String name,
            String description
    ) {
        return importData(tourApiContentId, sourceModifiedAt, name, description, "A01010400", "1", "5", null);
    }

    private static TouristSpotImportData importData(
            Long tourApiContentId,
            LocalDateTime sourceModifiedAt,
            String name,
            String description,
            String smallCategoryCode,
            String areaCode,
            String sigunguCode,
            String regionName
    ) {
        return new TouristSpotImportData(
                tourApiContentId,
                12,
                name,
                description,
                "Seoul Gwanak-gu",
                "trail",
                "08826",
                description == null ? null : new BigDecimal("37.4484036407"),
                description == null ? null : new BigDecimal("126.9540987991"),
                "02-0000-0000",
                "Always open",
                null,
                "https://example.com",
                null,
                "https://example.com/image.jpg",
                "https://example.com/thumb.jpg",
                LocalDateTime.of(2004, 1, 1, 0, 0),
                sourceModifiedAt,
                smallCategoryCode == null ? null : "A01",
                smallCategoryCode == null ? null : "A0101",
                smallCategoryCode,
                null,
                null,
                null,
                areaCode,
                sigunguCode,
                regionName,
                "11",
                "620"
        );
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

    private static class InMemoryTouristSpotRepository implements TouristSpotRepository {

        private TouristSpot saved;

        private InMemoryTouristSpotRepository(TouristSpot saved) {
            this.saved = saved;
        }

        @Override
        public Optional<TouristSpot> findByTourApiContentId(Long tourApiContentId) {
            if (saved != null && tourApiContentId.equals(saved.getTourApiContentId())) {
                return Optional.of(saved);
            }
            return Optional.empty();
        }

        @Override
        public TouristSpot save(TouristSpot touristSpot) {
            this.saved = touristSpot;
            return touristSpot;
        }
    }

    private static class NoopCategoryRepository implements CategoryRepository {

        @Override
        public Optional<Category> findByContentTypeIdAndSmallCategoryCode(
                Integer contentTypeId,
                String smallCategoryCode
        ) {
            return Optional.empty();
        }

        @Override
        public Category save(Category category) {
            return category;
        }
    }

    private static class NoopRegionRepository implements RegionRepository {

        @Override
        public Optional<Region> findByAreaCodeAndSigunguCode(String areaCode, String sigunguCode) {
            return Optional.empty();
        }

        @Override
        public Region save(Region region) {
            return region;
        }
    }
}
