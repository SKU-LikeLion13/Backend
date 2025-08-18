package AiGEN.AiGEN.service;

import lombok.Data;

import java.util.List;

@Data
public class ExplainRequest {
    /*내부 입력*/
    private String anonId;
    private Long batchId;
    private String dateRange;

    private List<PlatformKpi> platforms; // (A) 플랫폼 합계
    private String platformCode;         // (B) 특정 플랫폼 월별
    private List<MonthlyKpi> monthly;    // (B)

    @Data
    public static class PlatformKpi {
        private String platformCode;
        private double cpc;   // 원
        private double cvr;   // %
        private double roas;  // %
        private double roi;   // %
    }

    @Data
    public static class MonthlyKpi {
        private int year;
        private int month;
        private double cpc;   // 원
        private double cvr;   // %
        private double roas;  // %
        private double roi;   // %
    }
}
