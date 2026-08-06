package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalyzer;
import com.klist.chatbot.search.application.TouristSpotRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ChatOrchestrationConfiguration {

    @Bean
    ChatSearchOrchestrator chatSearchOrchestrator(
            ChatQuestionAnalyzer questionAnalyzer,
            TouristSpotRetriever touristSpotRetriever
    ) {
        return new ChatSearchOrchestrator(questionAnalyzer, touristSpotRetriever);
    }
}
