package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalysis;
import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalyzer;
import com.klist.chatbot.search.application.TouristSpotRetriever;
import com.klist.chatbot.search.application.TouristSpotSearchResult;
import java.util.Objects;

public class ChatSearchOrchestrator {

    private final ChatQuestionAnalyzer questionAnalyzer;
    private final TouristSpotRetriever touristSpotRetriever;

    public ChatSearchOrchestrator(
            ChatQuestionAnalyzer questionAnalyzer,
            TouristSpotRetriever touristSpotRetriever
    ) {
        this.questionAnalyzer = Objects.requireNonNull(
                questionAnalyzer,
                "questionAnalyzer must not be null"
        );
        this.touristSpotRetriever = Objects.requireNonNull(
                touristSpotRetriever,
                "touristSpotRetriever must not be null"
        );
    }

    public ChatSearchResult search(String question) {
        ChatQuestionAnalysis analysis = questionAnalyzer.analyze(question);
        TouristSpotSearchResult searchResult = touristSpotRetriever.retrieve(analysis.searchCriteria());
        return new ChatSearchResult(analysis, searchResult);
    }
}
