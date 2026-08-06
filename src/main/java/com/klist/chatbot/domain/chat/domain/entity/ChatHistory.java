package com.klist.chatbot.domain.chat.domain.entity;

import com.klist.chatbot.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "chat_history",
        indexes = {
                @Index(name = "idx_chat_history_session_created_at", columnList = "session_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_history_id")
    private Long id;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "question", nullable = false, columnDefinition = "text")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "text")
    private String answer;

    @Builder
    private ChatHistory(String sessionId, String question, String answer) {
        this.sessionId = sessionId;
        this.question = question;
        this.answer = answer;
    }
}
