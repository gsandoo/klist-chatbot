package com.klist.chatbot.chat.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalysis;
import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalyzer;
import com.klist.chatbot.search.application.TouristSpotRetriever;
import com.klist.chatbot.search.application.TouristSpotSearchCriteria;
import com.klist.chatbot.search.application.TouristSpotSearchResult;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatSearchOrchestratorTest {

    private final ChatQuestionAnalyzer questionAnalyzer = mock(ChatQuestionAnalyzer.class);
    private final TouristSpotRetriever touristSpotRetriever = mock(TouristSpotRetriever.class);
    private final ChatSearchOrchestrator orchestrator = new ChatSearchOrchestrator(
            questionAnalyzer,
            touristSpotRetriever
    );

    @Test
    void analyzesQuestionAndRetrievesTouristSpotsWithGeneratedCriteria() {
        String question = "서울에서 야경 명소 추천해줘";
        TouristSpotSearchCriteria criteria = criteria();
        ChatQuestionAnalysis analysis = new ChatQuestionAnalysis(
                question,
                "야경 명소",
                "서울",
                null,
                criteria
        );
        TouristSpotSearchResult searchResult = new TouristSpotSearchResult(
                List.of(),
                0,
                Duration.ofMillis(3)
        );
        when(questionAnalyzer.analyze(question)).thenReturn(analysis);
        when(touristSpotRetriever.retrieve(criteria)).thenReturn(searchResult);

        ChatSearchResult result = orchestrator.search(question);

        assertThat(result.questionAnalysis()).isSameAs(analysis);
        assertThat(result.touristSpotSearchResult()).isSameAs(searchResult);
        verify(questionAnalyzer).analyze(question);
        verify(touristSpotRetriever).retrieve(criteria);
    }

    @Test
    void preservesEmptySearchResultForFollowingOrchestrationSteps() {
        TouristSpotSearchCriteria criteria = criteria();
        ChatQuestionAnalysis analysis = new ChatQuestionAnalysis(
                "없는 장소",
                "없는 장소",
                null,
                null,
                criteria
        );
        TouristSpotSearchResult emptyResult = new TouristSpotSearchResult(
                List.of(),
                0,
                Duration.ZERO
        );
        when(questionAnalyzer.analyze("없는 장소")).thenReturn(analysis);
        when(touristSpotRetriever.retrieve(criteria)).thenReturn(emptyResult);

        ChatSearchResult result = orchestrator.search("없는 장소");

        assertThat(result.touristSpotSearchResult().isEmpty()).isTrue();
    }

    @Test
    void propagatesQuestionAnalysisFailureWithoutSearching() {
        IllegalArgumentException failure = new IllegalArgumentException("question must not be blank.");
        when(questionAnalyzer.analyze(" ")).thenThrow(failure);

        assertThatThrownBy(() -> orchestrator.search(" ")).isSameAs(failure);
    }

    @Test
    void requiresCollaboratorsAndResultValues() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ChatSearchOrchestrator(null, touristSpotRetriever))
                .withMessage("questionAnalyzer must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new ChatSearchOrchestrator(questionAnalyzer, null))
                .withMessage("touristSpotRetriever must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new ChatSearchResult(null, mock(TouristSpotSearchResult.class)))
                .withMessage("questionAnalysis must not be null");
    }

    private static TouristSpotSearchCriteria criteria() {
        return new TouristSpotSearchCriteria(
                "야경 명소", null, "1", null, null, null, null, null, null,
                null, null, null, 5, 0.1f
        );
    }
}
