package AiGEN.AiGEN.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "creative_request")
public class CreativeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anon_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserSession userSession;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Lob
    @Column(name = "prompt", nullable = false, columnDefinition = "LONGTEXT")
    private String prompt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    public CreativeRequest(UserSession userSession, String brandName,  String prompt) {
        this.userSession = userSession;
        this.brandName = brandName;
        this.prompt = prompt;
    }

}
