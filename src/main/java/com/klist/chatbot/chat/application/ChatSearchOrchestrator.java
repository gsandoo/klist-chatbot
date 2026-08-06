package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalysis;
import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalyzer;
import com.klist.chatbot.chat.application.evidence.ChatEvidenceContext;
import com.klist.chatbot.chat.application.evidence.ChatSearchEvidenceOrganizer;
import com.klist.chatbot.search.application.TouristSpotRetriever;
import com.klist.chatbot.search.application.TouristSpotSearchResult;
import java.util.Objects;

public class ChatSearchOrchestrator {

    private final ChatQuestionAnalyzer questionAnalyzer;
    private final TouristSpotRetriever touristSpotRetriever;
    private final ChatSearchEvidenceOrganizer evidenceOrganizer;

    public ChatSearchOrchestrator(
            ChatQuestionAnalyzer questionAnalyzer,
            TouristSpotRetriever touristSpotRetriever,
            ChatSearchEvidenceOrganizer evidenceOrganizer
    ) {
        this.questionAnalyzer = Objects.requireNonNull(
                questionAnalyzer,
                "questionAnalyzer must not be null"
        );
        this.touristSpotRetriever = Objects.requireNonNull(
                touristSpotRetriever,
                "touristSpotRetriever must not be null"
        );
        this.evidenceOrganizer = Objects.requireNonNull(
                evidenceOrganizer,
                "evidenceOrganizer must not be null"
        );
    }

    public ChatSearchResult search(String question) {
        ChatQuestionAnalysis analysis = questionAnalyzer.analyze(question);
        TouristSpotSearchResult searchResult = touristSpotRetriever.retrieve(analysis.searchCriteria());
        ChatEvidenceContext evidenceContext = evidenceOrganizer.organize(searchResult);
        return new ChatSearchResult(analysis, searchResult, evidenceContext);
    }
}
