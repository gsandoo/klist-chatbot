package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.answer.ChatGeneratedAnswer;
import com.klist.chatbot.chat.application.llm.LlmGenerationResult;
import java.util.Objects;
import java.util.Optional;

public record ChatCompletionResult(
        ChatCompletionStatus status,
        ChatSearchResult searchResult,
        LlmGenerationResult generationResult,
        ChatGeneratedAnswer generatedAnswer
) {

    public ChatCompletionResult {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(searchResult, "searchResult must not be null");
        if (status == ChatCompletionStatus.COMPLETED
                && (generationResult == null || generatedAnswer == null)) {
            throw new IllegalArgumentException(
                    "COMPLETED result requires generationResult and generatedAnswer"
            );
        }
        if (status == ChatCompletionStatus.NO_EVIDENCE
                && (generationResult != null || generatedAnswer != null)) {
            throw new IllegalArgumentException(
                    "NO_EVIDENCE result must not contain generated values"
            );
        }
    }

    public static ChatCompletionResult completed(
            ChatSearchResult searchResult,
            LlmGenerationResult generationResult,
            ChatGeneratedAnswer generatedAnswer
    ) {
        return new ChatCompletionResult(
                ChatCompletionStatus.COMPLETED,
                searchResult,
                generationResult,
                generatedAnswer
        );
    }

    public static ChatCompletionResult noEvidence(ChatSearchResult searchResult) {
        return new ChatCompletionResult(
                ChatCompletionStatus.NO_EVIDENCE,
                searchResult,
                null,
                null
        );
    }

    public Optional<LlmGenerationResult> optionalGenerationResult() {
        return Optional.ofNullable(generationResult);
    }

    public Optional<ChatGeneratedAnswer> optionalGeneratedAnswer() {
        return Optional.ofNullable(generatedAnswer);
    }
}
