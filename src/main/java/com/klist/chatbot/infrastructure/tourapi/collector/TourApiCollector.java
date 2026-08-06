package com.klist.chatbot.infrastructure.tourapi.collector;

import com.klist.chatbot.infrastructure.tourapi.client.TourApiClient;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClientResult;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClientStatus;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TourApiCollector {

    private static final Set<String> SUPPORTED_CONTENT_TYPES =
            Set.of("12", "14", "15", "25", "28", "32", "38", "39");

    private final TourApiClient tourApiClient;

    public TourApiCollector(TourApiClient tourApiClient) {
        this.tourApiClient = tourApiClient;
    }

    public TourApiCollectResult collect(TourApiCollectRequest request) {
        TourApiClientResult<TourApiAreaBasedListItem> areaResult =
                request != null && request.areaBasedListItem() != null
                        ? TourApiClientResult.success(request.areaBasedListItem())
                        : tourApiClient.getAreaBasedList(request);
        if (areaResult == null || !areaResult.isSuccess()) {
            return requiredFailure(request, areaResult);
        }

        TourApiAreaBasedListItem areaItem = areaResult.value();
        String contentId = areaItem.contentid();
        String contentTypeId = areaItem.contenttypeid();
        if (!SUPPORTED_CONTENT_TYPES.contains(contentTypeId)) {
            return unsupportedContentType(contentId, contentTypeId, areaItem);
        }

        EnumSet<TourApiEndpoint> missingEndpoints = EnumSet.noneOf(TourApiEndpoint.class);
        List<TourApiCollectWarning> warnings = new ArrayList<>();

        TourApiDetailCommonItem commonItem = optionalValue(
                TourApiEndpoint.DETAIL_COMMON,
                tourApiClient.getDetailCommon(contentId),
                missingEndpoints,
                warnings
        );
        TourApiDetailIntroItem introItem = optionalValue(
                TourApiEndpoint.DETAIL_INTRO,
                tourApiClient.getDetailIntro(contentId, contentTypeId),
                missingEndpoints,
                warnings
        );
        TourApiDetailImageItem imageItem = optionalValue(
                TourApiEndpoint.DETAIL_IMAGE,
                tourApiClient.getDetailImage(contentId),
                missingEndpoints,
                warnings
        );
        TourApiCodeItem regionCodeItem = optionalValue(
                TourApiEndpoint.REGION_CODE,
                tourApiClient.getRegionCode(areaItem.areacode(), areaItem.sigungucode()),
                missingEndpoints,
                warnings
        );

        TourApiCollectStatus status = missingEndpoints.isEmpty()
                ? TourApiCollectStatus.SUCCESS
                : TourApiCollectStatus.PARTIAL;
        return new TourApiCollectResult(
                contentId,
                contentTypeId,
                status,
                areaItem,
                commonItem,
                introItem,
                imageItem,
                regionCodeItem,
                missingEndpoints,
                warnings
        );
    }

    private TourApiCollectResult requiredFailure(
            TourApiCollectRequest request,
            TourApiClientResult<TourApiAreaBasedListItem> result
    ) {
        TourApiCollectWarningCode code = result != null && result.status() == TourApiClientStatus.EMPTY
                ? TourApiCollectWarningCode.REQUIRED_API_EMPTY
                : TourApiCollectWarningCode.REQUIRED_API_FAILURE;
        String reason = result == null ? "Client returned no result contract." : result.reason();
        return new TourApiCollectResult(
                request == null ? null : request.contentId(),
                null,
                TourApiCollectStatus.FAILED,
                null,
                null,
                null,
                null,
                null,
                Set.of(TourApiEndpoint.AREA_BASED_LIST),
                List.of(new TourApiCollectWarning(code, TourApiEndpoint.AREA_BASED_LIST, reason))
        );
    }

    private TourApiCollectResult unsupportedContentType(
            String contentId,
            String contentTypeId,
            TourApiAreaBasedListItem areaItem
    ) {
        return new TourApiCollectResult(
                contentId,
                contentTypeId,
                TourApiCollectStatus.FAILED,
                areaItem,
                null,
                null,
                null,
                null,
                Set.of(),
                List.of(new TourApiCollectWarning(
                        TourApiCollectWarningCode.UNSUPPORTED_CONTENT_TYPE,
                        TourApiEndpoint.AREA_BASED_LIST,
                        "Unsupported TourAPI contentTypeId: " + contentTypeId
                ))
        );
    }

    private <T> T optionalValue(
            TourApiEndpoint endpoint,
            TourApiClientResult<T> result,
            Set<TourApiEndpoint> missingEndpoints,
            List<TourApiCollectWarning> warnings
    ) {
        if (result != null && result.isSuccess()) {
            return result.value();
        }

        missingEndpoints.add(endpoint);
        boolean empty = result != null && result.status() == TourApiClientStatus.EMPTY;
        warnings.add(new TourApiCollectWarning(
                empty ? TourApiCollectWarningCode.OPTIONAL_API_EMPTY
                        : TourApiCollectWarningCode.OPTIONAL_API_FAILURE,
                endpoint,
                result == null ? "Client returned no result contract." : result.reason()
        ));
        return null;
    }
}
