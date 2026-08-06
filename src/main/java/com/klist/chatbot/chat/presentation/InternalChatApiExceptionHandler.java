package com.klist.chatbot.chat.presentation;

import com.klist.chatbot.chat.application.ChatQueryTimeoutException;
import com.klist.chatbot.chat.application.ChatProcessingFailedException;
import com.klist.chatbot.chat.application.ChatProcessingUnavailableException;
import com.klist.chatbot.chat.presentation.error.InternalApiErrorResponse;
import com.klist.chatbot.chat.presentation.error.InternalApiErrorResponse.FieldViolation;
import com.klist.chatbot.chat.presentation.error.InternalChatApiErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = InternalChatQueryController.class)
public class InternalChatApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<InternalApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String traceId = traceId(request);
        List<FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        InternalApiErrorResponse response = new InternalApiErrorResponse(
                InternalChatApiErrorCode.INVALID_REQUEST.name(),
                "The request is invalid.",
                traceId,
                java.time.Instant.now(),
                violations
        );
        return response(HttpStatus.BAD_REQUEST, traceId, response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<InternalApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return response(
                HttpStatus.BAD_REQUEST,
                traceId,
                InternalApiErrorResponse.of(
                        InternalChatApiErrorCode.INVALID_REQUEST.name(),
                        "The request body is invalid.",
                        traceId
                )
        );
    }

    @ExceptionHandler(ChatQueryTimeoutException.class)
    ResponseEntity<InternalApiErrorResponse> handleTimeout(
            ChatQueryTimeoutException exception,
            HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return response(
                HttpStatus.GATEWAY_TIMEOUT,
                traceId,
                InternalApiErrorResponse.of(
                        InternalChatApiErrorCode.CHAT_QUERY_TIMEOUT.name(),
                        "The chatbot query timed out.",
                        traceId
                )
        );
    }

    @ExceptionHandler(ChatProcessingUnavailableException.class)
    ResponseEntity<InternalApiErrorResponse> handleUnavailable(
            ChatProcessingUnavailableException exception,
            HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return response(
                HttpStatus.SERVICE_UNAVAILABLE,
                traceId,
                InternalApiErrorResponse.of(
                        InternalChatApiErrorCode.CHAT_PROCESSING_UNAVAILABLE.name(),
                        "Chat processing is not available.",
                        traceId
                )
        );
    }

    @ExceptionHandler(ChatProcessingFailedException.class)
    ResponseEntity<InternalApiErrorResponse> handleProcessingFailed(
            ChatProcessingFailedException exception,
            HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                traceId,
                InternalApiErrorResponse.of(
                        InternalChatApiErrorCode.CHAT_PROCESSING_FAILED.name(),
                        "The chatbot response could not be validated.",
                        traceId
                )
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<InternalApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                traceId,
                InternalApiErrorResponse.of(
                        InternalChatApiErrorCode.INTERNAL_ERROR.name(),
                        "The chatbot request could not be processed.",
                        traceId
                )
        );
    }

    private FieldViolation toViolation(FieldError error) {
        return new FieldViolation(error.getField(), error.getDefaultMessage());
    }

    private String traceId(HttpServletRequest request) {
        Object attribute = request.getAttribute(TraceIdResolver.REQUEST_ATTRIBUTE);
        if (attribute instanceof String value) {
            return value;
        }
        String traceId = TraceIdResolver.resolve(request.getHeader(TraceIdResolver.HEADER_NAME));
        request.setAttribute(TraceIdResolver.REQUEST_ATTRIBUTE, traceId);
        return traceId;
    }

    private ResponseEntity<InternalApiErrorResponse> response(
            HttpStatus status,
            String traceId,
            InternalApiErrorResponse body
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TraceIdResolver.HEADER_NAME, traceId);
        return new ResponseEntity<>(body, headers, status);
    }
}
