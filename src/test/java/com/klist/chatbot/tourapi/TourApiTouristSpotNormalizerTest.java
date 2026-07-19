package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingIssueCode;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiTouristSpotNormalizer;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class TourApiTouristSpotNormalizerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TourApiTouristSpotNormalizer NORMALIZER = new TourApiTouristSpotNormalizer();
    private static final TouristSpotImportMapper ENTITY_MAPPER = new TouristSpotImportMapper();
    private static final LocalDateTime LAST_SYNCED_AT = LocalDateTime.of(2026, 7, 19, 12, 0);

    @Test
    void normalizesAllSupportedContentTypes() throws Exception {
        List<JsonNode> samples = samples();

        for (JsonNode sample : samples) {
            TourApiMappingResult<TouristSpotImportData> result = normalize(sample);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.issues()).isEmpty();
            TouristSpotImportData data = result.value();
            assertThat(data.tourApiContentId()).isNotNull();
            assertThat(data.contentTypeId()).isIn(12, 14, 15, 25, 28, 32, 38, 39);
            assertThat(data.name()).isNotBlank();
            assertThat(data.address()).isNotBlank();
            assertThat(data.latitude()).isNotNull();
            assertThat(data.longitude()).isNotNull();
            assertThat(data.imageUrl()).isNotBlank();
            assertThat(data.thumbnailImageUrl()).isNotBlank();
            assertThat(data.sourceModifiedAt()).isNotNull();
            assertThat(data.openingHours()).isEqualTo(text(sample, "expectedOpeningHours"));
            assertThat(data.admissionFee()).isEqualTo(nullableText(sample, "expectedAdmissionFee"));
            assertThat(data.reservationUrl()).isEqualTo(nullableText(sample, "expectedReservationUrl"));

            TourApiMappingResult<TouristSpot> entityResult = ENTITY_MAPPER.toEntity(data, LAST_SYNCED_AT);
            assertThat(entityResult.isSuccess()).isTrue();
            assertThat(entityResult.value().getTourApiContentId()).isEqualTo(data.tourApiContentId());
            assertThat(entityResult.value().getCategoryId()).isNull();
            assertThat(entityResult.value().getRegionId()).isNull();
        }
    }

    @Test
    void cleansHtmlDescriptionAndExtractsHomepageHref() throws Exception {
        TourApiMappingResult<TouristSpotImportData> result = normalize(samples().get(0));

        assertThat(result.value().description()).isEqualTo("Mountain near Seoul.");
        assertThat(result.value().officialUrl()).isEqualTo("https://korean.visitseoul.net/");
        assertThat(result.value().tel()).isNull();
    }

    @Test
    void ignoresInvalidHomepageAndReservationGuideText() throws Exception {
        TourApiMappingResult<TouristSpotImportData> restaurant = normalize(samples().get(7));
        TourApiMappingResult<TouristSpotImportData> leports = normalize(samples().get(4));

        assertThat(restaurant.value().officialUrl()).isNull();
        assertThat(restaurant.value().reservationUrl()).isNull();
        assertThat(leports.value().reservationUrl()).isNull();
    }

    @Test
    void invalidCoordinateDoesNotFailMappingAndBecomesNull() throws Exception {
        TourApiAreaBasedListItem list = listItem(samples().get(0));
        TourApiAreaBasedListItem badCoordinate = new TourApiAreaBasedListItem(
                list.contentid(), list.contenttypeid(), list.title(), list.addr1(), list.addr2(), list.zipcode(),
                list.areacode(), list.sigungucode(), list.cat1(), list.cat2(), list.cat3(),
                list.lDongRegnCd(), list.lDongSignguCd(), list.lclsSystm1(), list.lclsSystm2(), list.lclsSystm3(),
                "999.0", "bad-latitude", list.tel(), list.firstimage(), list.firstimage2(),
                list.createdtime(), list.modifiedtime()
        );
        TourApiDetailCommonItem common = commonItem(samples().get(0));
        TourApiDetailCommonItem badCommonCoordinate = new TourApiDetailCommonItem(
                common.contentid(), common.contenttypeid(), common.title(), common.homepage(), common.overview(),
                common.addr1(), common.addr2(), common.zipcode(), "999.0", "bad-latitude", common.tel(),
                common.firstimage(), common.firstimage2(), common.createdtime(), common.modifiedtime()
        );

        TourApiMappingResult<TouristSpotImportData> result = NORMALIZER.normalize(
                badCoordinate, badCommonCoordinate, introItem(samples().get(0)), imageItem(samples().get(0)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value().latitude()).isNull();
        assertThat(result.value().longitude()).isNull();
        assertThat(result.issues()).extracting(issue -> issue.code())
                .containsExactly(TourApiMappingIssueCode.INVALID_COORDINATE, TourApiMappingIssueCode.INVALID_COORDINATE);
    }

    @Test
    void emptyCreatedTimeBecomesNull() throws Exception {
        TourApiAreaBasedListItem list = listItem(samples().get(0));
        TourApiAreaBasedListItem emptyListCreatedTime = new TourApiAreaBasedListItem(
                list.contentid(), list.contenttypeid(), list.title(), list.addr1(), list.addr2(), list.zipcode(),
                list.areacode(), list.sigungucode(), list.cat1(), list.cat2(), list.cat3(),
                list.lDongRegnCd(), list.lDongSignguCd(), list.lclsSystm1(), list.lclsSystm2(), list.lclsSystm3(),
                list.mapx(), list.mapy(), list.tel(), list.firstimage(), list.firstimage2(),
                "", list.modifiedtime()
        );
        TourApiDetailCommonItem common = commonItem(samples().get(0));
        TourApiDetailCommonItem emptyCreatedTime = new TourApiDetailCommonItem(
                common.contentid(), common.contenttypeid(), common.title(), common.homepage(), common.overview(),
                common.addr1(), common.addr2(), common.zipcode(), common.mapx(), common.mapy(), common.tel(),
                common.firstimage(), common.firstimage2(), "", common.modifiedtime()
        );

        TourApiMappingResult<TouristSpotImportData> result = NORMALIZER.normalize(
                emptyListCreatedTime, emptyCreatedTime, introItem(samples().get(0)), imageItem(samples().get(0)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value().sourceCreatedAt()).isNull();
    }

    @Test
    void invalidOptionalCreatedTimeProducesIssueButDoesNotFail() throws Exception {
        TourApiDetailCommonItem common = commonItem(samples().get(0));
        TourApiDetailCommonItem invalidCreatedTime = new TourApiDetailCommonItem(
                common.contentid(), common.contenttypeid(), common.title(), common.homepage(), common.overview(),
                common.addr1(), common.addr2(), common.zipcode(), common.mapx(), common.mapy(), common.tel(),
                common.firstimage(), common.firstimage2(), "2026-01-01", common.modifiedtime()
        );

        TourApiMappingResult<TouristSpotImportData> result = NORMALIZER.normalize(
                listItem(samples().get(0)), invalidCreatedTime, introItem(samples().get(0)), imageItem(samples().get(0)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.value().sourceCreatedAt()).isNull();
        assertThat(result.issues()).extracting(issue -> issue.code())
                .contains(TourApiMappingIssueCode.INVALID_DATE_TIME);
    }

    @Test
    void invalidRequiredModifiedTimeFailsMapping() throws Exception {
        TourApiDetailCommonItem common = commonItem(samples().get(0));
        TourApiDetailCommonItem invalidModifiedTime = new TourApiDetailCommonItem(
                common.contentid(), common.contenttypeid(), common.title(), common.homepage(), common.overview(),
                common.addr1(), common.addr2(), common.zipcode(), common.mapx(), common.mapy(), common.tel(),
                common.firstimage(), common.firstimage2(), common.createdtime(), "2026-01-01"
        );

        TourApiMappingResult<TouristSpotImportData> result = NORMALIZER.normalize(
                listItem(samples().get(0)), invalidModifiedTime, introItem(samples().get(0)), imageItem(samples().get(0)));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.issues()).extracting(issue -> issue.code())
                .contains(TourApiMappingIssueCode.INVALID_DATE_TIME);
    }

    @Test
    void missingRequiredContentIdFailsMapping() throws Exception {
        TourApiAreaBasedListItem list = listItem(samples().get(0));
        TourApiAreaBasedListItem missingContentId = new TourApiAreaBasedListItem(
                "", list.contenttypeid(), list.title(), list.addr1(), list.addr2(), list.zipcode(),
                list.areacode(), list.sigungucode(), list.cat1(), list.cat2(), list.cat3(),
                list.lDongRegnCd(), list.lDongSignguCd(), list.lclsSystm1(), list.lclsSystm2(), list.lclsSystm3(),
                list.mapx(), list.mapy(), list.tel(), list.firstimage(), list.firstimage2(),
                list.createdtime(), list.modifiedtime()
        );

        TourApiMappingResult<TouristSpotImportData> result = NORMALIZER.normalize(
                missingContentId, commonItem(samples().get(0)), introItem(samples().get(0)), imageItem(samples().get(0)));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.issues()).extracting(issue -> issue.code())
                .contains(TourApiMappingIssueCode.MISSING_REQUIRED_CONTENT_ID);
    }

    @Test
    void unsupportedContentTypeFailsMapping() throws Exception {
        TourApiAreaBasedListItem list = listItem(samples().get(0));
        TourApiAreaBasedListItem unsupported = new TourApiAreaBasedListItem(
                list.contentid(), "99", list.title(), list.addr1(), list.addr2(), list.zipcode(),
                list.areacode(), list.sigungucode(), list.cat1(), list.cat2(), list.cat3(),
                list.lDongRegnCd(), list.lDongSignguCd(), list.lclsSystm1(), list.lclsSystm2(), list.lclsSystm3(),
                list.mapx(), list.mapy(), list.tel(), list.firstimage(), list.firstimage2(),
                list.createdtime(), list.modifiedtime()
        );

        TourApiMappingResult<TouristSpotImportData> result = NORMALIZER.normalize(
                unsupported, commonItem(samples().get(0)), introItem(samples().get(0)), imageItem(samples().get(0)));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.issues()).extracting(issue -> issue.code())
                .contains(TourApiMappingIssueCode.UNSUPPORTED_CONTENT_TYPE_ID);
    }

    private static TourApiMappingResult<TouristSpotImportData> normalize(JsonNode sample) throws Exception {
        return NORMALIZER.normalize(listItem(sample), commonItem(sample), introItem(sample), imageItem(sample));
    }

    private static TourApiAreaBasedListItem listItem(JsonNode sample) throws Exception {
        return OBJECT_MAPPER.treeToValue(sample.get("areaBasedList2"), TourApiAreaBasedListItem.class);
    }

    private static TourApiDetailCommonItem commonItem(JsonNode sample) throws Exception {
        return OBJECT_MAPPER.treeToValue(sample.get("detailCommon2"), TourApiDetailCommonItem.class);
    }

    private static TourApiDetailIntroItem introItem(JsonNode sample) throws Exception {
        return OBJECT_MAPPER.treeToValue(sample.get("detailIntro2"), TourApiDetailIntroItem.class);
    }

    private static TourApiDetailImageItem imageItem(JsonNode sample) throws Exception {
        return OBJECT_MAPPER.treeToValue(sample.get("detailImage2"), TourApiDetailImageItem.class);
    }

    private static List<JsonNode> samples() throws Exception {
        try (InputStream inputStream = TourApiTouristSpotNormalizerTest.class
                .getResourceAsStream("/tourapi/tour-api-normalization-fixture.json")) {
            return OBJECT_MAPPER.readValue(inputStream, new TypeReference<>() {
            });
        }
    }

    private static String text(JsonNode node, String fieldName) {
        return node.get(fieldName).asText();
    }

    private static String nullableText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
