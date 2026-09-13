package com.klist.chatbot.chat.application.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EnglishQuestionAnalyzerTest {
    private final ChatQuestionAnalyzer analyzer = new ChatQuestionAnalyzer(new ChatQuestionAnalysisProperties());

    @ParameterizedTest
    @CsvSource({
            "Recommend places to visit in Seoul,seoul",
            "Tell me about Gyeongbokgung Palace,gyeongbokgung palace",
            "What are the opening hours of Gyeongbokgung Palace?,gyeongbokgung palace",
            "When does Gyeongbokgung Palace open?,gyeongbokgung palace"
    })
    void extractsEnglishSearchTerms(String question, String keyword) {
        var criteria = analyzer.analyze(question, "en").searchCriteria();
        assertThat(criteria.language()).isEqualTo("en");
        assertThat(criteria.keyword()).isEqualTo(keyword);
        assertThat(criteria.areaCode()).isNull();
    }

    @Test void usesEnglishContentTypeCodes() {
        var criteria = analyzer.analyze("Recommend museums in Seoul", "en").searchCriteria();
        assertThat(criteria.contentTypeId()).isEqualTo(78);
        assertThat(criteria.keyword()).isEqualTo("seoul");
    }
}
