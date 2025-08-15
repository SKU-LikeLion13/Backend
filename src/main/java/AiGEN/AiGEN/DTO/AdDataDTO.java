package AiGEN.AiGEN.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AdDataDTO {
    /** 원본 레코드 응답 (단일 행) */
    @Data
    @AllArgsConstructor
    public static class RowRes {
        private Long id;
        private Long batchId;
        private String anonId;
        private String platformCode;
        private LocalDate date;
        private long cost;
        private int clicks;
        private int conversions;
        private long revenue;
    }

    /** 플랫폼별 합계 + 지표 응답 */
    @Data
    @AllArgsConstructor
    public static class PlatformTotalsRes {
        private String platformCode;
        private long costSum;
        private int clicksSum;
        private int convSum;
        private long revenueSum;
        private BigDecimal cpc;   // cost / clicks
        private BigDecimal cvr;   // conv / clicks
        private BigDecimal roas;  // revenue / cost
        private BigDecimal roi;   // (revenue - cost) / cost
    }

    /** 특정 플랫폼의 월별 합계 + 지표 응답 */
    @Data
    @AllArgsConstructor
    public static class MonthlyTotalsRes {
        private int year;
        private int month;
        private long costSum;
        private int clicksSum;
        private int convSum;
        private long revenueSum;
        private BigDecimal cpc;
        private BigDecimal cvr;
        private BigDecimal roas;
        private BigDecimal roi;
    }

    @Data
    @AllArgsConstructor
    public static class PlatformInfoRes {
        private String code;
        private String name;
    }
}
