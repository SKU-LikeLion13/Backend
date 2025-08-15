package AiGEN.AiGEN.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class UserSession {

    @Id
    @Column(name = "anon_id", nullable=false)
    private String anonId;

    @CreatedDate
    @Column(name = "created_at", nullable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_seen", nullable=false)
    private LocalDateTime lastSeen;

    public UserSession(String anonId) {
        this.anonId = anonId;
    }
}
