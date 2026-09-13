package com.klist.chatbot.infrastructure.search.mapper;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.infrastructure.search.document.TouristSpotSearchCategory;
import com.klist.chatbot.infrastructure.search.document.TouristSpotSearchDocument;
import com.klist.chatbot.infrastructure.search.document.TouristSpotSearchRegion;
import java.util.Objects;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

@Component
public class TouristSpotSearchDocumentMapper {

    public TouristSpotSearchDocument map(TouristSpot touristSpot) {
        Objects.requireNonNull(touristSpot, "touristSpot must not be null");
        if (touristSpot.getId() == null) {
            throw new IllegalArgumentException("touristSpotId must not be null");
        }

        return new TouristSpotSearchDocument(
                touristSpot.getId(),
                touristSpot.getName(),
                touristSpot.getDescription(),
                touristSpot.getAddress(),
                region(touristSpot),
                category(touristSpot),
                coordinates(touristSpot),
                touristSpot.getImageUrl(),
                touristSpot.getTel(),
                touristSpot.getOpeningHours(),
                touristSpot.getAdmissionFee(),
                touristSpot.getReservationUrl(),
                touristSpot.getSourceModifiedAt(), touristSpot.getLanguage()
        );
    }

    private TouristSpotSearchRegion region(TouristSpot touristSpot) {
        return new TouristSpotSearchRegion(
                touristSpot.getRegionId(),
                touristSpot.getAreaCode(),
                touristSpot.getSigunguCode(),
                touristSpot.getLegalDongRegionCode(),
                touristSpot.getLegalDongSigunguCode()
        );
    }

    private TouristSpotSearchCategory category(TouristSpot touristSpot) {
        return new TouristSpotSearchCategory(
                touristSpot.getCategoryId(),
                touristSpot.getContentTypeId(),
                touristSpot.getLargeCategoryCode(),
                touristSpot.getMiddleCategoryCode(),
                touristSpot.getSmallCategoryCode()
        );
    }

    private GeoPoint coordinates(TouristSpot touristSpot) {
        if (touristSpot.getLatitude() == null || touristSpot.getLongitude() == null) {
            return null;
        }
        return new GeoPoint(
                touristSpot.getLatitude().doubleValue(),
                touristSpot.getLongitude().doubleValue()
        );
    }
}
