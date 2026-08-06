package com.klist.chatbot.chat.presentation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InternalChatTraceFilter extends OncePerRequestFilter {

    static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = TraceIdResolver.resolve(request.getHeader(TraceIdResolver.HEADER_NAME));
        String previousTraceId = MDC.get(MDC_KEY);
        request.setAttribute(TraceIdResolver.REQUEST_ATTRIBUTE, traceId);
        response.setHeader(TraceIdResolver.HEADER_NAME, traceId);
        MDC.put(MDC_KEY, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            restorePreviousTraceId(previousTraceId);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.equals("/internal/chat") || path.startsWith("/internal/chat/"));
    }

    private static void restorePreviousTraceId(String previousTraceId) {
        if (previousTraceId == null) {
            MDC.remove(MDC_KEY);
        } else {
            MDC.put(MDC_KEY, previousTraceId);
        }
    }
}
