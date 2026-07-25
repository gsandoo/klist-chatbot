package com.klist.chatbot.infrastructure.tourapi.collector;

import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import java.util.List;
import java.util.Set;

public record TourApiCollectResult(
        String contentId,
        String contentTypeId,
        TourApiCollectStatus status,
        TourApiAreaBasedListItem areaBasedListItem,
        TourApiDetailCommonItem detailCommonItem,
        TourApiDetailIntroItem detailIntroItem,
        TourApiDetailImageItem detailImageItem,
        TourApiCodeItem regionCodeItem,
        Set<TourApiEndpoint> missingEndpoints,
        List<TourApiCollectWarning> warnings
) {

    public TourApiCollectResult {
        missingEndpoints = Set.copyOf(missingEndpoints);
        warnings = List.copyOf(warnings);
    }

    public boolean isCollectable() {
        return status != TourApiCollectStatus.FAILED;
    }
}
