package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.infrastructure.search.document.TouristSpotSearchDocument;
import com.klist.chatbot.infrastructure.search.mapper.TouristSpotSearchDocumentMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.test.util.ReflectionTestUtils;

class TouristSpotSearchDocumentMapperTest {

    private final TouristSpotSearchDocumentMapper mapper = new TouristSpotSearchDocumentMapper();

    @Test
    void mapsTouristSpotEntityToSearchDocument() {
        LocalDateTime sourceModifiedAt = LocalDateTime.of(2026, 7, 27, 10, 30);
        TouristSpot entity = touristSpot(sourceModifiedAt, new BigDecimal("37.4484036407"),
                new BigDecimal("126.9540987991"));
        ReflectionTestUtils.setField(entity, "id", 101L);

        TouristSpotSearchDocument document = mapper.map(entity);

        assertThat(document.touristSpotId()).isEqualTo(101L);
        assertThat(document.title()).isEqualTo("관악산");
        assertThat(document.description()).isEqualTo("서울의 대표적인 산책 관광지");
        assertThat(document.address()).isEqualTo("서울특별시 관악구 관악로");
        assertThat(document.region().regionId()).isEqualTo(11L);
        assertThat(document.region().areaCode()).isEqualTo("1");
        assertThat(document.region().sigunguCode()).isEqualTo("5");
        assertThat(document.region().legalDongRegionCode()).isEqualTo("11");
        assertThat(document.region().legalDongSigunguCode()).isEqualTo("620");
        assertThat(document.category().categoryId()).isEqualTo(22L);
        assertThat(document.category().contentTypeId()).isEqualTo(12);
        assertThat(document.category().largeCategoryCode()).isEqualTo("A01");
        assertThat(document.category().middleCategoryCode()).isEqualTo("A0101");
        assertThat(document.category().smallCategoryCode()).isEqualTo("A01010400");
        assertThat(document.coordinates().getLat()).isEqualTo(37.4484036407);
        assertThat(document.coordinates().getLon()).isEqualTo(126.9540987991);
        assertThat(document.imageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(document.phoneNumber()).isEqualTo("02-1234-5678");
        assertThat(document.openingHours()).isEqualTo("매일 09:00~18:00");
        assertThat(document.admissionFee()).isEqualTo("무료");
        assertThat(document.reservationUrl()).isEqualTo("https://example.com/reservations");
        assertThat(document.sourceModifiedAt()).isEqualTo(sourceModifiedAt);
    }

    @Test
    void omitsCoordinatesWhenEitherCoordinateIsMissing() {
        TouristSpot entity = touristSpot(
                LocalDateTime.of(2026, 7, 27, 10, 30),
                new BigDecimal("37.4484036407"),
                null
        );
        ReflectionTestUtils.setField(entity, "id", 101L);

        TouristSpotSearchDocument document = mapper.map(entity);

        assertThat(document.coordinates()).isNull();
    }

    @Test
    void rejectsEntityWithoutDocumentId() {
        TouristSpot entity = touristSpot(
                LocalDateTime.of(2026, 7, 27, 10, 30),
                new BigDecimal("37.4484036407"),
                new BigDecimal("126.9540987991")
        );

        assertThatThrownBy(() -> mapper.map(entity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("touristSpotId must not be null");
    }

    @Test
    void declaresSearchFilterSortAndGeoFieldTypes() throws Exception {
        Document document = TouristSpotSearchDocument.class.getAnnotation(Document.class);
        assertThat(document.indexName()).isEqualTo("tourist-spots");
        assertThat(document.createIndex()).isFalse();

        MultiField title = field("title").getAnnotation(MultiField.class);
        assertThat(title.mainField().type()).isEqualTo(FieldType.Text);
        assertThat(title.otherFields()).singleElement()
                .satisfies(keyword -> {
                    assertThat(keyword.suffix()).isEqualTo("keyword");
                    assertThat(keyword.type()).isEqualTo(FieldType.Keyword);
                });

        assertFieldType("description", FieldType.Text);
        assertFieldType("region", FieldType.Object);
        assertFieldType("category", FieldType.Object);
        assertThat(field("coordinates").getAnnotation(GeoPointField.class)).isNotNull();
        assertFieldType("phoneNumber", FieldType.Keyword);
        assertFieldType("sourceModifiedAt", FieldType.Date);

        Field imageUrl = field("imageUrl").getAnnotation(Field.class);
        Field reservationUrl = field("reservationUrl").getAnnotation(Field.class);
        assertThat(imageUrl.index()).isFalse();
        assertThat(reservationUrl.index()).isFalse();
    }

    private static java.lang.reflect.Field field(String name) throws NoSuchFieldException {
        return TouristSpotSearchDocument.class.getDeclaredField(name);
    }

    private static void assertFieldType(String name, FieldType expected) throws NoSuchFieldException {
        assertThat(field(name).getAnnotation(Field.class).type()).isEqualTo(expected);
    }

    private static TouristSpot touristSpot(
            LocalDateTime sourceModifiedAt,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return TouristSpot.builder()
                .tourApiContentId(126480L)
                .contentTypeId(12)
                .categoryId(22L)
                .regionId(11L)
                .largeCategoryCode("A01")
                .middleCategoryCode("A0101")
                .smallCategoryCode("A01010400")
                .areaCode("1")
                .sigunguCode("5")
                .legalDongRegionCode("11")
                .legalDongSigunguCode("620")
                .name("관악산")
                .description("서울의 대표적인 산책 관광지")
                .address("서울특별시 관악구 관악로")
                .detailAddress("대학동")
                .zipCode("08826")
                .latitude(latitude)
                .longitude(longitude)
                .tel("02-1234-5678")
                .openingHours("매일 09:00~18:00")
                .admissionFee("무료")
                .officialUrl("https://example.com")
                .reservationUrl("https://example.com/reservations")
                .imageUrl("https://example.com/image.jpg")
                .thumbnailImageUrl("https://example.com/thumbnail.jpg")
                .sourceCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .sourceModifiedAt(sourceModifiedAt)
                .lastSyncedAt(LocalDateTime.of(2026, 7, 27, 11, 0))
                .build();
    }
}
