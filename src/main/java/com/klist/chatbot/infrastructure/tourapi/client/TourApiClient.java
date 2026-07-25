package com.klist.chatbot.infrastructure.tourapi.client;

import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectRequest;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;

public interface TourApiClient {

    default TourApiClientResult<TourApiPage<TourApiAreaBasedListItem>> getAreaBasedListPage(
            int pageNo,
            int numOfRows
    ) {
        return TourApiClientResult.failure("page query is not supported");
    }

    TourApiClientResult<TourApiAreaBasedListItem> getAreaBasedList(TourApiCollectRequest request);

    TourApiClientResult<TourApiDetailCommonItem> getDetailCommon(String contentId);

    TourApiClientResult<TourApiDetailIntroItem> getDetailIntro(String contentId, String contentTypeId);

    TourApiClientResult<TourApiDetailImageItem> getDetailImage(String contentId);

    TourApiClientResult<TourApiCodeItem> getRegionCode(String areaCode, String sigunguCode);
}
