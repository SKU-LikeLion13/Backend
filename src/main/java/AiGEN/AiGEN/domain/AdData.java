package AiGEN.AiGEN.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class AdData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ad_data_batch"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UploadBatch batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anon_id", referencedColumnName = "anon_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ad_data_session"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserSession userSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_code", referencedColumnName = "code", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ad_data_platform"))
    private AdPlatform platform;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "cost", nullable = false)
    private Long cost;

    @Column(name = "clicks", nullable = false)
    private Integer clicks;

    @Column(name = "conversions", nullable = false)
    private Integer conversions;

    @Column(name = "revenue", nullable = false)
    private Long revenue;

    public AdData(UploadBatch batch, UserSession userSession, AdPlatform platform, LocalDate date,
                  long cost, int clicks, int conversions, long revenue) {
        this.batch = batch;
        this.userSession = userSession;
        this.platform = platform;
        this.date = date;
        this.cost = cost;
        this.clicks = clicks;
        this.conversions = conversions;
        this.revenue = revenue;
    }
}
