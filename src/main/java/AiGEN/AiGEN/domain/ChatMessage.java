package AiGEN.AiGEN.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anon_id", nullable = false)
    private String anonId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(nullable = false, length = 16)
    private String role;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ChatMessage(String anonId, Long batchId, String role, String content) {
        this.anonId = anonId;
        this.batchId = batchId;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
