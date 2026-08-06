package com.klist.chatbot.chat.application;

public class ChatProcessingFailedException extends RuntimeException {

    public ChatProcessingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
