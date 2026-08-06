package com.klist.chatbot.domain.region.domain.entity;

import com.klist.chatbot.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "region",
        indexes = {
                @Index(name = "idx_region_parent_region_code", columnList = "parent_region_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_region_code",
                        columnNames = "region_code"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long id;

    @Column(name = "area_code", nullable = false, length = 20)
    private String areaCode;

    @Column(name = "sigungu_code", length = 20)
    private String sigunguCode;

    @Column(name = "region_code", nullable = false, length = 50)
    private String regionCode;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "parent_region_code", length = 20)
    private String parentRegionCode;

    @Builder
    private Region(
            String areaCode,
            String sigunguCode,
            String regionName,
            String parentRegionCode
    ) {
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.regionCode = buildRegionCode(areaCode, sigunguCode);
        this.regionName = regionName;
        this.parentRegionCode = parentRegionCode;
    }

    @PrePersist
    @PreUpdate
    private void syncRegionCode() {
        this.regionCode = buildRegionCode(this.areaCode, this.sigunguCode);
    }

    private static String buildRegionCode(String areaCode, String sigunguCode) {
        if (sigunguCode == null || sigunguCode.isBlank()) {
            return areaCode;
        }
        return areaCode + ":" + sigunguCode;
    }
}
