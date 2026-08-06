package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klist.chatbot.search.application.TouristSpotSearchCriteria;
import org.junit.jupiter.api.Test;

class TouristSpotSearchCriteriaTest {

    @Test
    void trimsKeywordAndAcceptsFilterOnlySearch() {
        TouristSpotSearchCriteria keyword = criteria("  서울 야경  ", null, null, null, 10, 0.5f);
        TouristSpotSearchCriteria filterOnly = criteria(null, 11L, null, null, 5, null);

        assertThat(keyword.keyword()).isEqualTo("서울 야경");
        assertThat(keyword.hasKeyword()).isTrue();
        assertThat(filterOnly.hasKeyword()).isFalse();
    }

    @Test
    void rejectsSearchWithoutAnyCondition() {
        assertThatThrownBy(() -> criteria(" ", null, null, null, 10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one search condition is required.");
    }

    @Test
    void rejectsPartialOrInvalidDistanceCondition() {
        assertThatThrownBy(() -> criteria("궁궐", null, 37.5, null, 10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be provided together");
        assertThatThrownBy(() -> criteria("궁궐", null, 91.0, 127.0, 10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitude");
    }

    @Test
    void limitsResultSizeAndMinimumScore() {
        assertThatThrownBy(() -> criteria("궁궐", null, null, null, 51, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> criteria("궁궐", null, null, null, 10, -0.1f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimumScore");
    }

    private static TouristSpotSearchCriteria criteria(
            String keyword,
            Long regionId,
            Double latitude,
            Double longitude,
            int size,
            Float minimumScore
    ) {
        return new TouristSpotSearchCriteria(
                keyword, regionId, null, null, null, null, null, null, null,
                latitude, longitude, latitude == null && longitude == null ? null : 10.0,
                size, minimumScore
        );
    }
}
