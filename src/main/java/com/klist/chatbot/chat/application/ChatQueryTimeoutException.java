package com.klist.chatbot.chat.application;

public class ChatQueryTimeoutException extends RuntimeException {

    public ChatQueryTimeoutException(String message) {
        super(message);
    }

    public ChatQueryTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
