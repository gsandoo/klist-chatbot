package com.klist.chatbot.speech.application;

import java.time.Duration;

public interface SpeechToTextClient {

    String transcribe(SpeechAudio audio, Duration timeout);

    default String transcribe(SpeechAudio audio, Duration timeout, String language) {
        if (!"ko".equals(language)) {
            throw new IllegalArgumentException("This STT client does not support " + language);
        }
        return transcribe(audio, timeout);
    }
}
