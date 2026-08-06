package com.klist.chatbot.chat.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InternalChatTraceFilterTest {

    private final InternalChatTraceFilter filter = new InternalChatTraceFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void exposesTraceIdThroughRequestResponseAndMdcDuringChatRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/internal/chat/query"
        );
        request.addHeader(TraceIdResolver.HEADER_NAME, "trace-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (filteredRequest, filteredResponse) -> {
            assertThat(MDC.get(InternalChatTraceFilter.MDC_KEY)).isEqualTo("trace-001");
            assertThat(filteredRequest.getAttribute(TraceIdResolver.REQUEST_ATTRIBUTE))
                    .isEqualTo("trace-001");
        });

        assertThat(response.getHeader(TraceIdResolver.HEADER_NAME)).isEqualTo("trace-001");
        assertThat(MDC.get(InternalChatTraceFilter.MDC_KEY)).isNull();
    }

    @Test
    void generatesTraceIdAndRestoresPreviousMdcValue() throws Exception {
        MDC.put(InternalChatTraceFilter.MDC_KEY, "outer-trace");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/internal/chat/query"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (filteredRequest, filteredResponse) ->
                assertThat(MDC.get(InternalChatTraceFilter.MDC_KEY))
                        .isNotBlank()
                        .isNotEqualTo("outer-trace"));

        assertThat(response.getHeader(TraceIdResolver.HEADER_NAME)).isNotBlank();
        assertThat(MDC.get(InternalChatTraceFilter.MDC_KEY)).isEqualTo("outer-trace");
    }

    @Test
    void ignoresNonChatRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (filteredRequest, filteredResponse) ->
                assertThat(MDC.get(InternalChatTraceFilter.MDC_KEY)).isNull());

        assertThat(response.getHeader(TraceIdResolver.HEADER_NAME)).isNull();
    }
}
