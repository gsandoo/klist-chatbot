package com.klist.chatbot.chat.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class InternalChatQueryRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidRequestAndAppliesDefaultTimeout() {
        InternalChatQueryRequest request = new InternalChatQueryRequest(
                "session-001",
                "user-001",
                "서울 야경 명소를 추천해줘",
                null
        );

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.effectiveTimeoutMs()).isEqualTo(5000);
    }

    @Test
    void acceptsInclusiveTimeoutBoundaries() {
        InternalChatQueryRequest minimum = request("message", 100);
        InternalChatQueryRequest maximum = request("message", 30000);

        assertThat(validator.validate(minimum)).isEmpty();
        assertThat(validator.validate(maximum)).isEmpty();
        assertThat(minimum.effectiveTimeoutMs()).isEqualTo(100);
        assertThat(maximum.effectiveTimeoutMs()).isEqualTo(30000);
    }

    @Test
    void rejectsBlankIdentifiersAndMessage() {
        InternalChatQueryRequest request = new InternalChatQueryRequest(" ", "", "\t", 5000);

        assertThat(violatedFields(request))
                .containsExactlyInAnyOrder("sessionId", "userId", "message");
    }

    @Test
    void rejectsOversizedFieldsAndOutOfRangeTimeout() {
        InternalChatQueryRequest oversized = new InternalChatQueryRequest(
                "s".repeat(101),
                "u".repeat(101),
                "m".repeat(4001),
                30001
        );

        assertThat(violatedFields(oversized))
                .containsExactlyInAnyOrder("sessionId", "userId", "message", "timeoutMs");
        assertThat(violatedFields(request("message", 99))).containsExactly("timeoutMs");
    }

    private Set<String> violatedFields(InternalChatQueryRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static InternalChatQueryRequest request(String message, Integer timeoutMs) {
        return new InternalChatQueryRequest("session-001", "user-001", message, timeoutMs);
    }
}
