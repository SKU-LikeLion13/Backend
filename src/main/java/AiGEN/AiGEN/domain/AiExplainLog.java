//package AiGEN.AiGEN.domain;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@Table(name = "ai_explain_log")
//public class AiExplainLog {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "anon_id", nullable = false)
//    private String anonId;
//
//    @Column(name = "batch_id")
//    private Long batchId;
//
//    @Lob
//    @Column(name = "input_json", nullable = false)
//    private String inputJson;
//
//    @Lob
//    @Column(name = "output_json")
//    private String outputJson;
//
//    @Column(nullable = false, length = 16)
//    private String status; // OK | ERROR | PENDING
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    /** 생성 팩토리 */
//    public static AiExplainLog pending(String anonId, Long batchId, String inputJson) {
//        AiExplainLog e = new AiExplainLog();
//        e.anonId = anonId;
//        e.batchId = batchId;
//        e.inputJson = inputJson;
//        e.status = "PENDING";
//        return e;
//    }
//
//    /** 도메인 메서드: 상태/출력 동시 갱신 */
//    public void updateStatusAndOutput(String status, String outputJson) {
//        this.status = status;
//        this.outputJson = outputJson;
//    }
//
//    /** 편의 메서드 */
//    public void markOk(String outputJson) { updateStatusAndOutput("OK", outputJson); }
//    public void markError(String outputJson) { updateStatusAndOutput("ERROR", outputJson); }
//
//    @PrePersist
//    void onCreate() {
//        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
//    }
//}
