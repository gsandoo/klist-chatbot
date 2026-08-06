package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.domain.category.domain.entity.Category;
import com.klist.chatbot.domain.region.domain.entity.Region;
import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TourApiEntityMappingTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TOUR_API_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test
    void tourApiSamplesCanBeMappedToCurrentEntities() throws Exception {
        JsonNode samples = OBJECT_MAPPER.readTree(SAMPLE_JSON);
        List<TouristSpot> touristSpots = new ArrayList<>();

        for (JsonNode sample : samples) {
            JsonNode list = sample.get("areaBasedList2");
            JsonNode common = sample.get("detailCommon2");
            JsonNode intro = sample.get("detailIntro2");
            JsonNode image = sample.get("detailImage2");

            Category category = Category.builder()
                    .contentTypeId(requiredInt(list, "contenttypeid"))
                    .largeCategoryCode(requiredText(list, "cat1"))
                    .middleCategoryCode(requiredText(list, "cat2"))
                    .smallCategoryCode(requiredText(list, "cat3"))
                    .categoryName(null)
                    .build();

            Region region = Region.builder()
                    .areaCode(requiredText(list, "areacode"))
                    .sigunguCode(blankToNull(text(list, "sigungucode")))
                    .regionName(requiredText(sample.get("areaCode2"), "name"))
                    .parentRegionCode(requiredText(list, "areacode"))
                    .build();

            TouristSpot touristSpot = TouristSpot.builder()
                    .tourApiContentId(requiredLong(list, "contentid"))
                    .contentTypeId(requiredInt(list, "contenttypeid"))
                    .categoryId(1L)
                    .regionId(1L)
                    .largeCategoryCode(requiredText(list, "cat1"))
                    .middleCategoryCode(requiredText(list, "cat2"))
                    .smallCategoryCode(requiredText(list, "cat3"))
                    .areaCode(requiredText(list, "areacode"))
                    .sigunguCode(blankToNull(text(list, "sigungucode")))
                    .legalDongRegionCode(blankToNull(text(list, "lDongRegnCd")))
                    .legalDongSigunguCode(blankToNull(text(list, "lDongSignguCd")))
                    .name(requiredText(list, "title"))
                    .description(blankToNull(text(common, "overview")))
                    .address(blankToNull(text(common, "addr1")))
                    .detailAddress(blankToNull(text(common, "addr2")))
                    .zipCode(blankToNull(text(common, "zipcode")))
                    .latitude(decimalOrNull(text(common, "mapy")))
                    .longitude(decimalOrNull(text(common, "mapx")))
                    .tel(blankToNull(text(common, "tel")))
                    .openingHours(openingHours(list, intro))
                    .admissionFee(admissionFee(list, intro))
                    .officialUrl(extractUrl(blankToNull(firstNonBlank(text(intro, "eventhomepage"), text(common, "homepage")))))
                    .reservationUrl(extractUrl(blankToNull(firstNonBlank(text(intro, "reservationurl")))))
                    .imageUrl(blankToNull(firstNonBlank(text(common, "firstimage"), text(image, "originimgurl"))))
                    .thumbnailImageUrl(blankToNull(firstNonBlank(text(common, "firstimage2"), text(image, "smallimageurl"))))
                    .sourceCreatedAt(parseTourApiDateTime(requiredText(common, "createdtime")))
                    .sourceModifiedAt(parseTourApiDateTime(requiredText(common, "modifiedtime")))
                    .lastSyncedAt(LocalDateTime.of(2026, 7, 17, 17, 0))
                    .build();

            assertThat(category.getContentTypeId()).isEqualTo(touristSpot.getContentTypeId());
            assertThat(region.getAreaCode()).isEqualTo(touristSpot.getAreaCode());
            touristSpots.add(touristSpot);
        }

        assertThat(touristSpots).hasSize(3);
        assertThat(touristSpots)
                .extracting(TouristSpot::getTourApiContentId)
                .containsExactly(126480L, 990208L, 3035607L);
        assertThat(touristSpots)
                .allSatisfy(touristSpot -> {
                    assertThat(touristSpot.getName()).isNotBlank();
                    assertThat(touristSpot.getSourceModifiedAt()).isNotNull();
                    assertThat(touristSpot.getLatitude()).isNotNull();
                    assertThat(touristSpot.getLongitude()).isNotNull();
                    assertThat(touristSpot.getImageUrl()).isNotBlank();
                    assertThat(touristSpot.getThumbnailImageUrl()).isNotBlank();
                });
        assertThat(touristSpots.get(0).getOfficialUrl()).isEqualTo("https://korean.visitseoul.net/");
        assertThat(touristSpots.get(1).getReservationUrl()).isEqualTo("https://www.lottehotel.com/");
        assertThat(touristSpots.get(2).getOfficialUrl()).isEqualTo("https://example.or.kr/gwanghwamun-market");
    }

    private static String openingHours(JsonNode list, JsonNode intro) {
        return blankToNull(firstNonBlank(
                text(intro, "usetime"),
                combine("체크인 ", text(intro, "checkintime"), " / 체크아웃 ", text(intro, "checkouttime")),
                combine("행사기간 ", text(intro, "eventstartdate"), " ~ ", text(intro, "eventenddate")),
                text(list, "usetime")
        ));
    }

    private static String admissionFee(JsonNode list, JsonNode intro) {
        return blankToNull(firstNonBlank(
                text(intro, "usefee"),
                text(intro, "usefeeleports"),
                text(intro, "parkingfee"),
                text(intro, "saleitemcost"),
                text(intro, "usetimefestival"),
                text(list, "usefee")
        ));
    }

    private static String combine(String prefix, String first, String separator, String second) {
        if (blankToNull(first) == null && blankToNull(second) == null) {
            return null;
        }
        return prefix + blankToEmpty(first) + separator + blankToEmpty(second);
    }

    private static String text(JsonNode node, String fieldName) {
        if (node == null || node.get(fieldName) == null || node.get(fieldName).isNull()) {
            return null;
        }
        return node.get(fieldName).asText();
    }

    private static String requiredText(JsonNode node, String fieldName) {
        String value = blankToNull(text(node, fieldName));
        assertThat(value).as(fieldName).isNotNull();
        return value;
    }

    private static Long requiredLong(JsonNode node, String fieldName) {
        return Long.parseLong(requiredText(node, fieldName));
    }

    private static Integer requiredInt(JsonNode node, String fieldName) {
        return Integer.parseInt(requiredText(node, fieldName));
    }

    private static BigDecimal decimalOrNull(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        return new BigDecimal(normalized);
    }

    private static LocalDateTime parseTourApiDateTime(String value) {
        return LocalDateTime.parse(value, TOUR_API_DATE_TIME);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String blankToEmpty(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return "";
        }
        return normalized;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = blankToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private static String extractUrl(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        int hrefIndex = normalized.indexOf("href=\"");
        if (hrefIndex < 0) {
            return normalized;
        }
        int start = hrefIndex + "href=\"".length();
        int end = normalized.indexOf('"', start);
        if (end < 0) {
            return normalized;
        }
        return normalized.substring(start, end);
    }

    private static final String SAMPLE_JSON = """
            [
              {
                "areaBasedList2": {
                  "contentid": "126480",
                  "contenttypeid": "12",
                  "title": "관악산",
                  "addr1": "서울특별시 관악구 관악로",
                  "addr2": "대학동 일대",
                  "zipcode": "08826",
                  "areacode": "1",
                  "sigungucode": "5",
                  "cat1": "A01",
                  "cat2": "A0101",
                  "cat3": "A01010400",
                  "lDongRegnCd": "11",
                  "lDongSignguCd": "620",
                  "mapx": "126.9540987991",
                  "mapy": "37.4484036407",
                  "tel": "",
                  "firstimage": "http://tong.visitkorea.or.kr/cms/resource/74/3589374_image2_1.jpg",
                  "firstimage2": "http://tong.visitkorea.or.kr/cms/resource/74/3589374_image3_1.jpg",
                  "createdtime": "20040129090000",
                  "modifiedtime": "20260105111740"
                },
                "detailCommon2": {
                  "contentid": "126480",
                  "contenttypeid": "12",
                  "title": "관악산",
                  "homepage": "<a href=\\"https://korean.visitseoul.net/\\" target=\\"_blank\\">https://korean.visitseoul.net/</a>",
                  "overview": "서울 한강 남쪽에 솟아 있는 산으로 도심에서 가까운 대표 자연 관광지이다.",
                  "addr1": "서울특별시 관악구 관악로",
                  "addr2": "대학동 일대",
                  "zipcode": "08826",
                  "mapx": "126.9540987991",
                  "mapy": "37.4484036407",
                  "tel": "",
                  "firstimage": "http://tong.visitkorea.or.kr/cms/resource/74/3589374_image2_1.jpg",
                  "firstimage2": "http://tong.visitkorea.or.kr/cms/resource/74/3589374_image3_1.jpg",
                  "createdtime": "20040129090000",
                  "modifiedtime": "20260105111740"
                },
                "detailIntro2": {
                  "contentid": "126480",
                  "contenttypeid": "12",
                  "usetime": "상시 개방",
                  "restdate": "연중무휴"
                },
                "detailImage2": {
                  "contentid": "126480",
                  "originimgurl": "http://tong.visitkorea.or.kr/cms/resource/71/3589371_image2_1.jpg",
                  "smallimageurl": "http://tong.visitkorea.or.kr/cms/resource/71/3589371_image3_1.jpg"
                },
                "areaCode2": {
                  "code": "5",
                  "name": "서울특별시 관악구"
                }
              },
              {
                "areaBasedList2": {
                  "contentid": "990208",
                  "contenttypeid": "32",
                  "title": "롯데시티호텔 마포",
                  "addr1": "서울특별시 마포구 마포대로 109",
                  "addr2": "",
                  "zipcode": "",
                  "areacode": "1",
                  "sigungucode": "13",
                  "cat1": "B02",
                  "cat2": "B0201",
                  "cat3": "B02010100",
                  "lDongRegnCd": "11",
                  "lDongSignguCd": "440",
                  "mapx": "126.9505300000",
                  "mapy": "37.5445740000",
                  "tel": "",
                  "firstimage": "http://tong.visitkorea.or.kr/cms/resource/08/990208_image2_1.jpg",
                  "firstimage2": "http://tong.visitkorea.or.kr/cms/resource/08/990208_image3_1.jpg",
                  "createdtime": "20100101000000",
                  "modifiedtime": "20260105000000"
                },
                "detailCommon2": {
                  "contentid": "990208",
                  "contenttypeid": "32",
                  "title": "롯데시티호텔 마포",
                  "homepage": "https://www.lottehotel.com/",
                  "overview": "마포 지역에 위치한 비즈니스 및 관광 숙박시설이다.",
                  "addr1": "서울특별시 마포구 마포대로 109",
                  "addr2": "",
                  "zipcode": "",
                  "mapx": "126.9505300000",
                  "mapy": "37.5445740000",
                  "tel": "",
                  "firstimage": "http://tong.visitkorea.or.kr/cms/resource/08/990208_image2_1.jpg",
                  "firstimage2": "http://tong.visitkorea.or.kr/cms/resource/08/990208_image3_1.jpg",
                  "createdtime": "20100101000000",
                  "modifiedtime": "20260105000000"
                },
                "detailIntro2": {
                  "contentid": "990208",
                  "contenttypeid": "32",
                  "checkintime": "15:00",
                  "checkouttime": "12:00",
                  "reservationlodging": "홈페이지 예약",
                  "reservationurl": "https://www.lottehotel.com/"
                },
                "detailImage2": {
                  "contentid": "990208",
                  "originimgurl": "http://tong.visitkorea.or.kr/cms/resource/09/990209_image2_1.jpg",
                  "smallimageurl": "http://tong.visitkorea.or.kr/cms/resource/09/990209_image3_1.jpg"
                },
                "areaCode2": {
                  "code": "13",
                  "name": "서울특별시 마포구"
                }
              },
              {
                "areaBasedList2": {
                  "contentid": "3035607",
                  "contenttypeid": "15",
                  "title": "광화문 마켓",
                  "addr1": "서울특별시 종로구 세종대로",
                  "addr2": "광화문광장 일대",
                  "zipcode": "",
                  "areacode": "1",
                  "sigungucode": "23",
                  "cat1": "A02",
                  "cat2": "A0207",
                  "cat3": "A02070200",
                  "lDongRegnCd": "11",
                  "lDongSignguCd": "110",
                  "mapx": "126.9769000000",
                  "mapy": "37.5729500000",
                  "tel": "",
                  "firstimage": "http://tong.visitkorea.or.kr/cms/resource/07/3035607_image2_1.jpg",
                  "firstimage2": "http://tong.visitkorea.or.kr/cms/resource/07/3035607_image3_1.jpg",
                  "createdtime": "20230101000000",
                  "modifiedtime": "20260105000000"
                },
                "detailCommon2": {
                  "contentid": "3035607",
                  "contenttypeid": "15",
                  "title": "광화문 마켓",
                  "homepage": "",
                  "overview": "광화문 일대에서 열리는 계절형 행사 및 마켓 콘텐츠이다.",
                  "addr1": "서울특별시 종로구 세종대로",
                  "addr2": "광화문광장 일대",
                  "zipcode": "",
                  "mapx": "126.9769000000",
                  "mapy": "37.5729500000",
                  "tel": "",
                  "firstimage": "http://tong.visitkorea.or.kr/cms/resource/07/3035607_image2_1.jpg",
                  "firstimage2": "http://tong.visitkorea.or.kr/cms/resource/07/3035607_image3_1.jpg",
                  "createdtime": "20230101000000",
                  "modifiedtime": "20260105000000"
                },
                "detailIntro2": {
                  "contentid": "3035607",
                  "contenttypeid": "15",
                  "eventstartdate": "20251213",
                  "eventenddate": "20260105",
                  "playtime": "18:00~22:00",
                  "usetimefestival": "무료",
                  "eventhomepage": "https://example.or.kr/gwanghwamun-market",
                  "bookingplace": ""
                },
                "detailImage2": {
                  "contentid": "3035607",
                  "originimgurl": "http://tong.visitkorea.or.kr/cms/resource/17/3035617_image2_1.jpg",
                  "smallimageurl": "http://tong.visitkorea.or.kr/cms/resource/17/3035617_image3_1.jpg"
                },
                "areaCode2": {
                  "code": "23",
                  "name": "서울특별시 종로구"
                }
              }
            ]
            """;
}
