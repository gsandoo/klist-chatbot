package com.klist.chatbot.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.klist.chatbot.chat.application.ChatCompletionResult;
import com.klist.chatbot.chat.application.ChatCompletionStatus;
import com.klist.chatbot.chat.application.ChatSearchResult;
import com.klist.chatbot.chat.application.llm.LlmGenerationResult;
import com.klist.chatbot.search.application.TouristSpotSearchResult;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MicrometerChatMetricsRecorderTest {

    @Test
    void recordsCompletedChatSearchLlmAndTokenMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerChatMetricsRecorder recorder = new MicrometerChatMetricsRecorder(registry);
        ChatCompletionResult completion = mock(ChatCompletionResult.class);
        ChatSearchResult search = mock(ChatSearchResult.class);
        TouristSpotSearchResult searchResult = mock(TouristSpotSearchResult.class);
        LlmGenerationResult generation = new LlmGenerationResult(
                "answer", "test-model", 120, 30, Duration.ofMillis(450)
        );
        when(completion.status()).thenReturn(ChatCompletionStatus.COMPLETED);
        when(completion.searchResult()).thenReturn(search);
        when(search.touristSpotSearchResult()).thenReturn(searchResult);
        when(searchResult.executionTime()).thenReturn(Duration.ofMillis(25));
        when(completion.optionalGenerationResult()).thenReturn(Optional.of(generation));

        recorder.completed(completion, Duration.ofMillis(600));

        assertThat(registry.get("chatbot.chat.duration").tag("status", "completed")
                .timer().totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(600);
        assertThat(registry.get("chatbot.search.duration").tag("status", "completed")
                .timer().totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(25);
        assertThat(registry.get("chatbot.llm.duration").tag("model", "test-model")
                .timer().totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(450);
        assertThat(registry.get("chatbot.llm.tokens").tag("type", "input")
                .counter().count()).isEqualTo(120);
        assertThat(registry.get("chatbot.llm.tokens").tag("type", "output")
                .counter().count()).isEqualTo(30);
    }

    @Test
    void recordsFailedChatWithBoundedReason() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerChatMetricsRecorder recorder = new MicrometerChatMetricsRecorder(registry);

        recorder.failed("timeout", Duration.ofMillis(500));

        assertThat(registry.get("chatbot.chat.duration").tag("status", "failed")
                .timer().count()).isEqualTo(1);
        assertThat(registry.get("chatbot.chat.failures").tag("reason", "timeout")
                .counter().count()).isEqualTo(1);
    }
}
