package com.klist.chatbot.infrastructure.tourapi.mapper;

import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TourApiTouristSpotNormalizer {

    public TourApiMappingResult<TouristSpotImportData> normalize(
            TourApiAreaBasedListItem listItem,
            TourApiDetailCommonItem commonItem,
            TourApiDetailIntroItem introItem,
            TourApiDetailImageItem imageItem
    ) {
        return normalize(listItem, commonItem, introItem, imageItem, null);
    }

    public TourApiMappingResult<TouristSpotImportData> normalize(
            TourApiAreaBasedListItem listItem,
            TourApiDetailCommonItem commonItem,
            TourApiDetailIntroItem introItem,
            TourApiDetailImageItem imageItem,
            TourApiCodeItem regionCodeItem
    ) {
        List<TourApiMappingIssue> issues = new ArrayList<>();
        Long contentId = requiredLong(listItem == null ? null : listItem.contentid(),
                "contentid", TourApiMappingIssueCode.MISSING_REQUIRED_CONTENT_ID,
                TourApiMappingIssueCode.INVALID_CONTENT_ID, issues);
        Integer contentTypeId = requiredInteger(firstNonBlank(
                        listItem == null ? null : listItem.contenttypeid(),
                        commonItem == null ? null : commonItem.contenttypeid(),
                        introItem == null ? null : introItem.contenttypeid()),
                "contenttypeid", issues);
        String name = requiredText(firstNonBlank(
                        commonItem == null ? null : commonItem.title(),
                        listItem == null ? null : listItem.title()),
                "title", TourApiMappingIssueCode.MISSING_REQUIRED_NAME, issues);

        TourApiIntroFieldPolicy policy = null;
        if (contentTypeId != null) {
            policy = TourApiIntroFieldPolicy.find(contentTypeId).orElse(null);
            if (policy == null) {
                issues.add(issue(TourApiMappingIssueCode.UNSUPPORTED_CONTENT_TYPE_ID,
                        "contenttypeid", "Unsupported TourAPI contentTypeId: " + contentTypeId));
            }
        }

        BigDecimal latitude = coordinate(firstNonBlank(
                commonItem == null ? null : commonItem.mapy(),
                listItem == null ? null : listItem.mapy()), "mapy", true, issues);
        BigDecimal longitude = coordinate(firstNonBlank(
                commonItem == null ? null : commonItem.mapx(),
                listItem == null ? null : listItem.mapx()), "mapx", false, issues);
        LocalDateTime sourceCreatedAt = dateTime(firstNonBlank(
                commonItem == null ? null : commonItem.createdtime(),
                listItem == null ? null : listItem.createdtime()), "createdtime", false, issues);
        LocalDateTime sourceModifiedAt = dateTime(firstNonBlank(
                commonItem == null ? null : commonItem.modifiedtime(),
                listItem == null ? null : listItem.modifiedtime()), "modifiedtime", true, issues);

        if (hasRequiredFailure(issues)) {
            return TourApiMappingResult.failure(issues);
        }

        String officialUrl = firstNonBlank(
                policy == null || introItem == null ? null : policy.officialUrl(introItem),
                TourApiDataCleaner.extractUrl(commonItem == null ? null : commonItem.homepage())
        );
        String reservationUrl = policy == null || introItem == null ? null : policy.reservationUrl(introItem);

        TouristSpotImportData data = new TouristSpotImportData(
                contentId,
                contentTypeId,
                name,
                TourApiDataCleaner.cleanHtmlText(commonItem == null ? null : commonItem.overview()),
                clean(firstNonBlank(commonItem == null ? null : commonItem.addr1(), listItem == null ? null : listItem.addr1())),
                clean(firstNonBlank(commonItem == null ? null : commonItem.addr2(), listItem == null ? null : listItem.addr2())),
                clean(firstNonBlank(commonItem == null ? null : commonItem.zipcode(), listItem == null ? null : listItem.zipcode())),
                latitude,
                longitude,
                clean(firstNonBlank(commonItem == null ? null : commonItem.tel(), listItem == null ? null : listItem.tel())),
                policy == null || introItem == null ? null : policy.openingHours(introItem),
                policy == null || introItem == null ? null : policy.admissionFee(introItem),
                officialUrl,
                reservationUrl,
                clean(firstNonBlank(commonItem == null ? null : commonItem.firstimage(),
                        listItem == null ? null : listItem.firstimage(),
                        imageItem == null ? null : imageItem.originimgurl())),
                clean(firstNonBlank(commonItem == null ? null : commonItem.firstimage2(),
                        listItem == null ? null : listItem.firstimage2(),
                        imageItem == null ? null : imageItem.smallimageurl())),
                sourceCreatedAt,
                sourceModifiedAt,
                clean(listItem == null ? null : listItem.cat1()),
                clean(listItem == null ? null : listItem.cat2()),
                clean(listItem == null ? null : listItem.cat3()),
                clean(listItem == null ? null : listItem.lclsSystm1()),
                clean(listItem == null ? null : listItem.lclsSystm2()),
                clean(listItem == null ? null : listItem.lclsSystm3()),
                clean(listItem == null ? null : listItem.areacode()),
                clean(listItem == null ? null : listItem.sigungucode()),
                clean(regionCodeItem == null ? null : regionCodeItem.name()),
                clean(listItem == null ? null : listItem.lDongRegnCd()),
                clean(listItem == null ? null : listItem.lDongSignguCd())
        );
        return TourApiMappingResult.success(data, issues);
    }

    private static String clean(String value) {
        return TourApiDataCleaner.normalizeText(value);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) {
                return cleaned;
            }
        }
        return null;
    }

    private static String requiredText(
            String value,
            String fieldName,
            TourApiMappingIssueCode missingCode,
            List<TourApiMappingIssue> issues
    ) {
        String cleaned = clean(value);
        if (cleaned == null) {
            issues.add(issue(missingCode, fieldName, "Required TourAPI field is missing."));
        }
        return cleaned;
    }

    private static Long requiredLong(
            String value,
            String fieldName,
            TourApiMappingIssueCode missingCode,
            TourApiMappingIssueCode invalidCode,
            List<TourApiMappingIssue> issues
    ) {
        String cleaned = requiredText(value, fieldName, missingCode, issues);
        if (cleaned == null) {
            return null;
        }
        try {
            return Long.valueOf(cleaned);
        } catch (NumberFormatException exception) {
            issues.add(issue(invalidCode, fieldName, "Required TourAPI numeric field is invalid."));
            return null;
        }
    }

    private static Integer requiredInteger(String value, String fieldName, List<TourApiMappingIssue> issues) {
        String cleaned = requiredText(value, fieldName,
                TourApiMappingIssueCode.MISSING_REQUIRED_CONTENT_TYPE_ID, issues);
        if (cleaned == null) {
            return null;
        }
        try {
            return Integer.valueOf(cleaned);
        } catch (NumberFormatException exception) {
            issues.add(issue(TourApiMappingIssueCode.INVALID_CONTENT_TYPE_ID,
                    fieldName, "Required TourAPI content type field is invalid."));
            return null;
        }
    }

    private static BigDecimal coordinate(
            String value,
            String fieldName,
            boolean latitude,
            List<TourApiMappingIssue> issues
    ) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(cleaned);
            BigDecimal min = BigDecimal.valueOf(latitude ? -90 : -180);
            BigDecimal max = BigDecimal.valueOf(latitude ? 90 : 180);
            if (decimal.compareTo(min) < 0 || decimal.compareTo(max) > 0) {
                issues.add(issue(TourApiMappingIssueCode.INVALID_COORDINATE,
                        fieldName, "Coordinate is outside the valid range."));
                return null;
            }
            return decimal;
        } catch (NumberFormatException exception) {
            issues.add(issue(TourApiMappingIssueCode.INVALID_COORDINATE,
                    fieldName, "Coordinate is not a decimal number."));
            return null;
        }
    }

    private static LocalDateTime dateTime(
            String value,
            String fieldName,
            boolean required,
            List<TourApiMappingIssue> issues
    ) {
        String cleaned = clean(value);
        if (cleaned == null) {
            if (required) {
                issues.add(issue(TourApiMappingIssueCode.MISSING_REQUIRED_SOURCE_MODIFIED_AT,
                        fieldName, "Required TourAPI modified time is missing."));
            }
            return null;
        }
        LocalDateTime parsed = TourApiDataCleaner.parseTourApiDateTime(cleaned);
        if (parsed == null) {
            issues.add(issue(TourApiMappingIssueCode.INVALID_DATE_TIME,
                    fieldName, "TourAPI datetime must use yyyyMMddHHmmss."));
        }
        return parsed;
    }

    private static boolean hasRequiredFailure(List<TourApiMappingIssue> issues) {
        return issues.stream().anyMatch(issue -> {
            if (issue.code() == TourApiMappingIssueCode.INVALID_DATE_TIME) {
                return "modifiedtime".equals(issue.fieldName());
            }
            return switch (issue.code()) {
                case MISSING_REQUIRED_CONTENT_ID,
                        MISSING_REQUIRED_CONTENT_TYPE_ID,
                        MISSING_REQUIRED_NAME,
                        MISSING_REQUIRED_SOURCE_MODIFIED_AT,
                        INVALID_CONTENT_ID,
                        INVALID_CONTENT_TYPE_ID,
                        UNSUPPORTED_CONTENT_TYPE_ID -> true;
                case INVALID_COORDINATE, INVALID_DATE_TIME, OPTIONAL_FIELD_MISSING -> false;
            };
        });
    }

    private static TourApiMappingIssue issue(TourApiMappingIssueCode code, String fieldName, String message) {
        return new TourApiMappingIssue(code, fieldName, message);
    }
}
