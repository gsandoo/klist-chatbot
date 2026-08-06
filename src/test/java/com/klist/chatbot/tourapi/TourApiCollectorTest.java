package com.klist.chatbot.tourapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.klist.chatbot.domain.touristspot.service.TouristSpotImporter;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClient;
import com.klist.chatbot.infrastructure.tourapi.client.TourApiClientResult;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectRequest;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectResult;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectStatus;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollectWarningCode;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiCollector;
import com.klist.chatbot.infrastructure.tourapi.collector.TourApiEndpoint;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiCodeItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailCommonItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailImageItem;
import com.klist.chatbot.infrastructure.tourapi.dto.TourApiDetailIntroItem;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class TourApiCollectorTest {

    @Test
    void returnsSuccessWhenAllApisSucceed() {
        FakeTourApiClient client = new FakeTourApiClient();

        TourApiCollectResult result = new TourApiCollector(client).collect(request());

        assertThat(result.status()).isEqualTo(TourApiCollectStatus.SUCCESS);
        assertThat(result.contentId()).isEqualTo("126480");
        assertThat(result.missingEndpoints()).isEmpty();
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void returnsWarningWhenDetailImageFails() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.imageResult = TourApiClientResult.failure("detailImage response format error");

        TourApiCollectResult result = new TourApiCollector(client).collect(request());

        assertThat(result.status()).isEqualTo(TourApiCollectStatus.PARTIAL);
        assertThat(result.detailImageItem()).isNull();
        assertThat(result.missingEndpoints()).containsExactly(TourApiEndpoint.DETAIL_IMAGE);
        assertThat(result.warnings()).anySatisfy(warning -> {
            assertThat(warning.code()).isEqualTo(TourApiCollectWarningCode.OPTIONAL_API_FAILURE);
            assertThat(warning.endpoint()).isEqualTo(TourApiEndpoint.DETAIL_IMAGE);
        });
    }

    @Test
    void returnsWarningWhenDetailIntroFails() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.introResult = TourApiClientResult.failure("detailIntro unavailable");

        TourApiCollectResult result = new TourApiCollector(client).collect(request());

        assertThat(result.status()).isEqualTo(TourApiCollectStatus.PARTIAL);
        assertThat(result.missingEndpoints()).contains(TourApiEndpoint.DETAIL_INTRO);
        assertThat(result.warnings()).extracting(warning -> warning.code())
                .contains(TourApiCollectWarningCode.OPTIONAL_API_FAILURE);
    }

    @Test
    void returnsFailedWhenAreaBasedListFails() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.areaResult = TourApiClientResult.failure("areaBasedList unavailable");

        TourApiCollectResult result = new TourApiCollector(client).collect(request());

        assertThat(result.status()).isEqualTo(TourApiCollectStatus.FAILED);
        assertThat(result.missingEndpoints()).containsExactly(TourApiEndpoint.AREA_BASED_LIST);
        assertThat(client.detailCallCount).isZero();
    }

    @Test
    void returnsFailedWhenRequiredResponseIsEmpty() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.areaResult = TourApiClientResult.empty("areaBasedList item is empty");

        TourApiCollectResult result = new TourApiCollector(client).collect(request());

        assertThat(result.status()).isEqualTo(TourApiCollectStatus.FAILED);
        assertThat(result.warnings()).extracting(warning -> warning.code())
                .containsExactly(TourApiCollectWarningCode.REQUIRED_API_EMPTY);
    }

    @Test
    void returnsFailedWithoutDetailCallsWhenContentTypeIsUnsupported() {
        FakeTourApiClient client = new FakeTourApiClient();
        client.areaResult = TourApiClientResult.success(areaItem("99"));

        TourApiCollectResult result = new TourApiCollector(client).collect(request());

        assertThat(result.status()).isEqualTo(TourApiCollectStatus.FAILED);
        assertThat(result.warnings()).extracting(warning -> warning.code())
                .containsExactly(TourApiCollectWarningCode.UNSUPPORTED_CONTENT_TYPE);
        assertThat(client.detailCallCount).isZero();
    }

    @Test
    void collectorHasNoImportServiceDependency() {
        assertThat(Arrays.stream(TourApiCollector.class.getDeclaredFields()))
                .noneMatch(field -> TouristSpotImporter.class.isAssignableFrom(field.getType()));
    }

    private static TourApiCollectRequest request() {
        return new TourApiCollectRequest("126480");
    }

    private static TourApiAreaBasedListItem areaItem(String contentTypeId) {
        return new TourApiAreaBasedListItem(
                "126480", contentTypeId, "Gwanaksan", "Seoul", null, "08826",
                "01", "005", "A01", "A0101", "A01010400",
                null, null, null, null, null, "126.9540987991", "37.4484036407", null,
                "https://example.com/image.jpg", "https://example.com/thumb.jpg",
                "20040101000000", "20260101000000"
        );
    }

    private static TourApiDetailCommonItem commonItem() {
        return new TourApiDetailCommonItem(
                "126480", "12", "Gwanaksan", null, "Description",
                "Seoul", null, "08826", "126.9540987991", "37.4484036407",
                null, null, null, "20040101000000", "20260101000000"
        );
    }

    private static TourApiDetailIntroItem introItem() {
        return new TourApiDetailIntroItem(
                "126480", "12", "Always open", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null
        );
    }

    private static class FakeTourApiClient implements TourApiClient {

        private TourApiClientResult<TourApiAreaBasedListItem> areaResult =
                TourApiClientResult.success(areaItem("12"));
        private TourApiClientResult<TourApiDetailCommonItem> commonResult =
                TourApiClientResult.success(commonItem());
        private TourApiClientResult<TourApiDetailIntroItem> introResult =
                TourApiClientResult.success(introItem());
        private TourApiClientResult<TourApiDetailImageItem> imageResult =
                TourApiClientResult.success(new TourApiDetailImageItem(
                        "126480", "image", "https://example.com/image.jpg",
                        "https://example.com/thumb.jpg", "1", "Type1"
                ));
        private TourApiClientResult<TourApiCodeItem> regionResult =
                TourApiClientResult.success(new TourApiCodeItem("005", "Gwanak-gu", "1"));
        private int detailCallCount;

        @Override
        public TourApiClientResult<TourApiAreaBasedListItem> getAreaBasedList(TourApiCollectRequest request) {
            return areaResult;
        }

        @Override
        public TourApiClientResult<TourApiDetailCommonItem> getDetailCommon(String contentId) {
            detailCallCount++;
            return commonResult;
        }

        @Override
        public TourApiClientResult<TourApiDetailIntroItem> getDetailIntro(
                String contentId,
                String contentTypeId
        ) {
            detailCallCount++;
            return introResult;
        }

        @Override
        public TourApiClientResult<TourApiDetailImageItem> getDetailImage(String contentId) {
            detailCallCount++;
            return imageResult;
        }

        @Override
        public TourApiClientResult<TourApiCodeItem> getRegionCode(String areaCode, String sigunguCode) {
            detailCallCount++;
            return regionResult;
        }
    }
}
