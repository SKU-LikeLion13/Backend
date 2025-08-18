package AiGEN.AiGEN.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ai_explain_log")
public class AiExplainLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anon_id", nullable = false)
    private String anonId;

    @Column(name = "batch_id")
    private Long batchId;

    @Lob
    @Column(name = "input_json", nullable = false, columnDefinition = "LONGTEXT")
    private String inputJson;

    @Lob
    @Column(name = "output_json", columnDefinition = "LONGTEXT")
    private String outputJson;

    @Setter
    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 생성 팩토리 */
    public AiExplainLog(String anonId, Long batchId, String inputJson, String outputJson) {
        this.anonId = anonId;
        this.batchId = batchId;
        this.inputJson = inputJson;
        this.outputJson = outputJson;
    }

    /** 도메인 메서드: 상태/출력 동시 갱신 */
    public void updateStatusAndOutput(String status, String outputJson) {
        this.status = status;
        this.outputJson = outputJson;
    }

    /** 편의 메서드 */
    public void markOk(String outputJson) { updateStatusAndOutput("OK", outputJson); }
    public void markError(String outputJson) { updateStatusAndOutput("ERROR", outputJson); }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
