package AiGEN.AiGEN.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class UploadBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anon_id", referencedColumnName = "anon_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_upload_batch_session"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserSession userSession;

    private String filename;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    public UploadBatch(UserSession userSession, String filename, LocalDateTime uploadedAt) {
        this.userSession = userSession;
        this.filename = filename;
        this.uploadedAt = uploadedAt;
    }
}
