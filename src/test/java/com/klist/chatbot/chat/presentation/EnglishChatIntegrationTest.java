package com.klist.chatbot.chat.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.klist.chatbot.chat.application.llm.LlmClient;
import com.klist.chatbot.chat.application.llm.LlmGenerationResult;
import com.klist.chatbot.search.application.*;
import com.klist.chatbot.speech.application.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"llm.openai.enabled=true", "llm.openai.api-key=test-api-key"})
@AutoConfigureMockMvc
class EnglishChatIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean TouristSpotSearchGateway search;
    @MockitoBean LlmClient llm;
    @MockitoBean SpeechToTextClient stt;

    private static final String REQUEST = """
            {"requestId":"a22c717d-5a3e-46b5-92fc-f41624b85887","sessionId":"english-session",
             "userId":"english-user","message":"Tell me about Gyeongbokgung Palace","language":"en"}
            """;

    private void prepareAnswer() {
        when(search.search(any(), any(Duration.class))).thenAnswer(invocation -> {
            TouristSpotSearchCriteria criteria = invocation.getArgument(0);
            assertThat(criteria.language()).isEqualTo("en");
            assertThat(criteria.keyword()).isEqualTo("gyeongbokgung palace");
            return new TouristSpotSearchResult(List.of(new TouristSpotSearchEvidence(
                    264337L, "Gyeongbokgung Palace", "A royal palace built in 1395.",
                    "161 Sajik-ro, Jongno-gu, Seoul", null, null, 76, 37.576, 126.976,
                    null, null, null, null, null, 8.5f)), 1, Duration.ZERO);
        });
        when(llm.generate(any())).thenAnswer(invocation -> {
            com.klist.chatbot.chat.application.llm.LlmGenerationRequest request = invocation.getArgument(0);
            assertThat(request.prompt().systemMessage()).contains("Response language: English");
            return new LlmGenerationResult("""
                    {"answer":"Gyeongbokgung Palace is a royal palace built in 1395.",
                     "recommendations":[{"touristSpotId":264337,"reason":"Explore a historic royal palace."}]}
                    """, "test-model", 20, 20, Duration.ZERO);
        });
    }

    @Test void englishTextFlowsThroughSearchPromptAndAnswer() throws Exception {
        prepareAnswer();
        mvc.perform(post("/internal/chat/query").header("X-Internal-Api-Key", "test-internal-api-key")
                        .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.answer").value("Gyeongbokgung Palace is a royal palace built in 1395."))
                .andExpect(jsonPath("$.sources[0].title").value("Gyeongbokgung Palace"));
        verify(search).search(any(), any(Duration.class));
        verify(llm).generate(any());
    }

    @Test void englishAudioSetsSttLanguageAndContinuesThroughEnglishChat() throws Exception {
        prepareAnswer();
        when(stt.transcribe(any(), any(), eq("en"))).thenReturn("Tell me about Gyeongbokgung Palace");
        mvc.perform(multipart("/internal/chat/query/audio")
                        .file(new MockMultipartFile("request", "request.json", "application/json", REQUEST.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("audio", "question.wav", "audio/wav", new byte[]{1, 2, 3}))
                        .header("X-Internal-Api-Key", "test-internal-api-key"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transcription").value("Tell me about Gyeongbokgung Palace"))
                .andExpect(jsonPath("$.answer").value("Gyeongbokgung Palace is a royal palace built in 1395."));
        verify(stt).transcribe(any(), any(), eq("en"));
    }

    @Test void rejectsUnsupportedLanguageBeforeSearching() throws Exception {
        mvc.perform(post("/internal/chat/query").header("X-Internal-Api-Key", "test-internal-api-key")
                        .contentType(MediaType.APPLICATION_JSON).content(REQUEST.replace("\"en\"", "\"fr\"")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(search, llm);
    }

    @Test void noResultsAreExplainedInEnglishWithoutLlm() throws Exception {
        when(search.search(any(), any(Duration.class))).thenReturn(new TouristSpotSearchResult(List.of(), 0, Duration.ZERO));
        mvc.perform(post("/internal/chat/query").header("X-Internal-Api-Key", "test-internal-api-key")
                        .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NO_RESULT"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.startsWith("I could not find")));
        verifyNoInteractions(llm);
    }
}
