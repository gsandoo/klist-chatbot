package com.klist.chatbot.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klist.chatbot.search.application.TouristSpotRetriever;
import com.klist.chatbot.search.application.TouristSpotSearchCriteria;
import com.klist.chatbot.search.application.TouristSpotSearchGateway;
import com.klist.chatbot.search.application.TouristSpotSearchResult;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class TouristSpotRetrieverTest {

    private final TouristSpotSearchGateway gateway = mock(TouristSpotSearchGateway.class);
    private final TouristSpotRetriever retriever = new TouristSpotRetriever(gateway);

    @Test
    void delegatesValidatedCriteriaToSearchGateway() {
        TouristSpotSearchCriteria criteria = criteria();
        TouristSpotSearchResult expected = new TouristSpotSearchResult(List.of(), 0, Duration.ofMillis(2));
        when(gateway.search(criteria)).thenReturn(expected);

        TouristSpotSearchResult actual = retriever.retrieve(criteria);

        assertThat(actual).isSameAs(expected);
        verify(gateway).search(criteria);
    }

    @Test
    void rejectsNullCriteria() {
        assertThatNullPointerException()
                .isThrownBy(() -> retriever.retrieve(null))
                .withMessage("criteria must not be null");
    }

    private static TouristSpotSearchCriteria criteria() {
        return new TouristSpotSearchCriteria(
                "서울 야경", null, null, null, null, null, null, null, null,
                null, null, null, 10, null
        );
    }
}
