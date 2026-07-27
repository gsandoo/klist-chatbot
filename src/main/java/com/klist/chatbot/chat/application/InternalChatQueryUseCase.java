package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.presentation.dto.InternalChatQueryRequest;
import com.klist.chatbot.chat.presentation.dto.InternalChatQueryResponse;

public interface InternalChatQueryUseCase {

    InternalChatQueryResponse query(InternalChatQueryRequest request, String traceId);
}
