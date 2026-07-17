package com.klist.chatbot.domain.touristspot.domain.entity;

import com.klist.chatbot.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "tourist_spot",
        indexes = {
                @Index(name = "idx_tourist_spot_content_type_id", columnList = "content_type_id"),
                @Index(name = "idx_tourist_spot_category_id", columnList = "category_id"),
                @Index(name = "idx_tourist_spot_region_id", columnList = "region_id"),
                @Index(name = "idx_tourist_spot_source_modified_at", columnList = "source_modified_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tourist_spot_tour_api_content_id",
                        columnNames = "tour_api_content_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TouristSpot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tourist_spot_id")
    private Long id;

    @Column(name = "tour_api_content_id", nullable = false)
    private Long tourApiContentId;

    @Column(name = "content_type_id", nullable = false)
    private Integer contentTypeId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "large_category_code", length = 20)
    private String largeCategoryCode;

    @Column(name = "middle_category_code", length = 20)
    private String middleCategoryCode;

    @Column(name = "small_category_code", length = 20)
    private String smallCategoryCode;

    @Column(name = "area_code", length = 20)
    private String areaCode;

    @Column(name = "sigungu_code", length = 20)
    private String sigunguCode;

    @Column(name = "legal_dong_region_code", length = 20)
    private String legalDongRegionCode;

    @Column(name = "legal_dong_sigungu_code", length = 20)
    private String legalDongSigunguCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "detail_address", length = 500)
    private String detailAddress;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "latitude", precision = 13, scale = 10)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 13, scale = 10)
    private BigDecimal longitude;

    @Column(name = "tel", length = 100)
    private String tel;

    @Column(name = "opening_hours", columnDefinition = "text")
    private String openingHours;

    @Column(name = "admission_fee", columnDefinition = "text")
    private String admissionFee;

    @Column(name = "official_url", length = 2048)
    private String officialUrl;

    @Column(name = "reservation_url", length = 2048)
    private String reservationUrl;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "thumbnail_image_url", length = 2048)
    private String thumbnailImageUrl;

    @Column(name = "source_created_at")
    private LocalDateTime sourceCreatedAt;

    @Column(name = "source_modified_at", nullable = false)
    private LocalDateTime sourceModifiedAt;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Builder
    private TouristSpot(
            Long tourApiContentId,
            Integer contentTypeId,
            Long categoryId,
            Long regionId,
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            String areaCode,
            String sigunguCode,
            String legalDongRegionCode,
            String legalDongSigunguCode,
            String name,
            String description,
            String address,
            String detailAddress,
            String zipCode,
            BigDecimal latitude,
            BigDecimal longitude,
            String tel,
            String openingHours,
            String admissionFee,
            String officialUrl,
            String reservationUrl,
            String imageUrl,
            String thumbnailImageUrl,
            LocalDateTime sourceCreatedAt,
            LocalDateTime sourceModifiedAt,
            LocalDateTime lastSyncedAt
    ) {
        this.tourApiContentId = tourApiContentId;
        this.contentTypeId = contentTypeId;
        this.categoryId = categoryId;
        this.regionId = regionId;
        this.largeCategoryCode = largeCategoryCode;
        this.middleCategoryCode = middleCategoryCode;
        this.smallCategoryCode = smallCategoryCode;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.legalDongRegionCode = legalDongRegionCode;
        this.legalDongSigunguCode = legalDongSigunguCode;
        this.name = name;
        this.description = description;
        this.address = address;
        this.detailAddress = detailAddress;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tel = tel;
        this.openingHours = openingHours;
        this.admissionFee = admissionFee;
        this.officialUrl = officialUrl;
        this.reservationUrl = reservationUrl;
        this.imageUrl = imageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.sourceCreatedAt = sourceCreatedAt;
        this.sourceModifiedAt = sourceModifiedAt;
        this.lastSyncedAt = lastSyncedAt;
    }
}
