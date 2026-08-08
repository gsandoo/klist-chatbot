package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.answer.ChatAnswerGroundingException;
import com.klist.chatbot.chat.application.answer.ChatGeneratedAnswer;
import com.klist.chatbot.chat.application.answer.ChatLlmResponseParsingException;
import com.klist.chatbot.chat.application.answer.ChatRecommendation;
import com.klist.chatbot.chat.application.evidence.ChatTouristSpotEvidence;
import com.klist.chatbot.chat.application.llm.LlmClientException;
import com.klist.chatbot.chat.application.llm.LlmFailureType;
import com.klist.chatbot.chat.presentation.dto.ChatQueryStatus;
import com.klist.chatbot.chat.presentation.dto.ChatSourceResponse;
import com.klist.chatbot.chat.presentation.dto.ChatSourceType;
import com.klist.chatbot.chat.presentation.dto.InternalChatQueryRequest;
import com.klist.chatbot.chat.presentation.dto.InternalChatQueryResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

public class InternalChatQueryService implements InternalChatQueryUseCase {

    private static final String NO_RESULT_ANSWER =
            "조건에 맞는 관광지를 찾지 못했습니다. 다른 지역이나 관광 유형으로 질문해 주세요.";

    private final ChatCompletionOrchestrator completionOrchestrator;
    private final LongSupplier nanoTime;
    private final ChatMetricsRecorder metrics;

    public InternalChatQueryService(ChatCompletionOrchestrator completionOrchestrator) {
        this(completionOrchestrator, System::nanoTime, ChatMetricsRecorder.NO_OP);
    }

    public InternalChatQueryService(
            ChatCompletionOrchestrator completionOrchestrator,
            ChatMetricsRecorder metrics
    ) {
        this(completionOrchestrator, System::nanoTime, metrics);
    }

    InternalChatQueryService(
            ChatCompletionOrchestrator completionOrchestrator,
            LongSupplier nanoTime
    ) {
        this(completionOrchestrator, nanoTime, ChatMetricsRecorder.NO_OP);
    }

    InternalChatQueryService(
            ChatCompletionOrchestrator completionOrchestrator,
            LongSupplier nanoTime,
            ChatMetricsRecorder metrics
    ) {
        this.completionOrchestrator = Objects.requireNonNull(
                completionOrchestrator,
                "completionOrchestrator must not be null"
        );
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime must not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
    }

    @Override
    public InternalChatQueryResponse query(InternalChatQueryRequest request, String traceId) {
        Objects.requireNonNull(request, "request must not be null");
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
        long startedAt = nanoTime.getAsLong();
        try {
            ChatCompletionResult completionResult = complete(request);
            Duration processingTime = elapsed(startedAt, nanoTime.getAsLong());
            InternalChatQueryResponse response = toResponse(
                    completionResult,
                    traceId,
                    processingTime.toMillis()
            );
            metrics.completed(completionResult, processingTime);
            return response;
        } catch (RuntimeException exception) {
            metrics.failed(
                    failureComponent(exception),
                    failureReason(exception),
                    elapsed(startedAt, nanoTime.getAsLong())
            );
            throw exception;
        }
    }

    private static InternalChatQueryResponse toResponse(
            ChatCompletionResult completionResult,
            String traceId,
            long processingTimeMs
    ) {
        if (completionResult.status() == ChatCompletionStatus.NO_EVIDENCE) {
            return new InternalChatQueryResponse(
                    NO_RESULT_ANSWER, List.of(), traceId, ChatQueryStatus.NO_RESULT, processingTimeMs
            );
        }
        ChatGeneratedAnswer generatedAnswer = completionResult.generatedAnswer();
        return new InternalChatQueryResponse(
                generatedAnswer.answer(),
                toSources(completionResult, generatedAnswer),
                traceId,
                ChatQueryStatus.COMPLETED,
                processingTimeMs
        );
    }

    private ChatCompletionResult complete(InternalChatQueryRequest request) {
        try {
            return completionOrchestrator.complete(
                    request.message(),
                    Duration.ofMillis(request.effectiveTimeoutMs())
            );
        } catch (LlmClientException exception) {
            throw translateLlmFailure(exception);
        } catch (ChatLlmResponseParsingException | ChatAnswerGroundingException exception) {
            throw new ChatProcessingFailedException("Chat response validation failed", exception);
        }
    }

    private RuntimeException translateLlmFailure(LlmClientException exception) {
        if (exception.failureType() == LlmFailureType.TIMEOUT) {
            return new ChatQueryTimeoutException("LLM request timed out", exception);
        }
        if (exception.failureType() == LlmFailureType.INVALID_RESPONSE
                || (exception.failureType() == LlmFailureType.HTTP && !exception.retryable())) {
            return new ChatProcessingFailedException("LLM response processing failed", exception);
        }
        return new ChatProcessingUnavailableException("LLM processing is unavailable", exception);
    }

    private static List<ChatSourceResponse> toSources(
            ChatCompletionResult completionResult,
            ChatGeneratedAnswer generatedAnswer
    ) {
        Map<Long, ChatTouristSpotEvidence> evidenceById = completionResult.searchResult()
                .evidenceContext()
                .touristSpots()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        ChatTouristSpotEvidence::touristSpotId,
                        evidence -> evidence
                ));
        return generatedAnswer.recommendations().stream()
                .map(recommendation -> toSource(recommendation, evidenceById))
                .toList();
    }

    private static ChatSourceResponse toSource(
            ChatRecommendation recommendation,
            Map<Long, ChatTouristSpotEvidence> evidenceById
    ) {
        ChatTouristSpotEvidence evidence = evidenceById.get(recommendation.touristSpotId());
        if (evidence == null) {
            throw new IllegalStateException("Validated recommendation evidence is missing");
        }
        return new ChatSourceResponse(
                evidence.touristSpotId(),
                evidence.title(),
                ChatSourceType.TOURIST_SPOT,
                Double.parseDouble(Float.toString(evidence.searchScore())),
                evidence.reservationUrl()
        );
    }

    private static Duration elapsed(long startedAt, long completedAt) {
        return Duration.ofNanos(Math.max(0, completedAt - startedAt));
    }

    private static String failureReason(RuntimeException exception) {
        if (exception instanceof ChatQueryTimeoutException) {
            return "timeout";
        }
        if (exception instanceof ChatProcessingUnavailableException) {
            return "unavailable";
        }
        if (exception instanceof ChatProcessingFailedException) {
            return "invalid_response";
        }
        return "unexpected";
    }

    private static String failureComponent(RuntimeException exception) {
        if (exception instanceof ChatProcessingUnavailableException
                || exception instanceof ChatProcessingFailedException) {
            return "llm";
        }
        if (exception instanceof ChatQueryTimeoutException
                && hasCause(exception, LlmClientException.class)) {
            return "llm";
        }
        return "chat";
    }

    private static boolean hasCause(Throwable failure, Class<? extends Throwable> type) {
        Throwable current = failure;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
