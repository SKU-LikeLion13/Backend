package AiGEN.AiGEN.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class AdPlatform {
    @Id
    @Column(name="code", length=40, nullable=false)
    private String code; // (PK)

    @Column(length = 80, nullable = false)
    private String name;

    @Column(nullable=false)
    private boolean active = true;

    public AdPlatform(String code, String name, boolean active) {
        this.code = code;
        this.name = name;
        this.active = active;
    }
}
