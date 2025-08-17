//package AiGEN.AiGEN.service;
//
//import AiGEN.AiGEN.DTO.AdDataDTO;
//import AiGEN.AiGEN.DTO.ExplainResponse;
//import AiGEN.AiGEN.domain.UserSession;
//import AiGEN.AiGEN.exception.NotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ExplainAggregationService {
//    /*집계 → 내부요청 변환*/
//
//    private final AdDataService adDataService;
//    private final KpiExplainService kpiExplainService;
//
//    /** (A) 플랫폼별 합계 KPI 설명 */
//    public ExplainResponse explainPlatformTotals(UserSession session, String dateRange) {
//        List<AdDataDTO.PlatformTotalsRes> totals = adDataService.reportByPlatformAll(session);
//        if (totals == null || totals.isEmpty()) throw new NotFoundException("설명을 생성할 집계 데이터가 없습니다.");
//
//        ExplainRequest req = new ExplainRequest();
//        req.setAnonId(session.getAnonId());
//        req.setDateRange(dateRange == null ? "전체 업로드 범위" : dateRange);
//        req.setPlatforms(
//                totals.stream().map(t -> {
//                    ExplainRequest.PlatformKpi p = new ExplainRequest.PlatformKpi();
//                    p.setPlatformCode(t.getPlatformCode());
//                    p.setCpc(toD(t.getCpc()));      // 원
//                    p.setCvr(toD(t.getCvr()));      // 이미 % 스케일
//                    p.setRoas(toD(t.getRoas()));    // 이미 % 스케일
//                    p.setRoi(toD(t.getRoi()));      // 이미 % 스케일
//                    return p;
//                }).toList()
//        );
//
//        return kpiExplainService.explain(req);
//    }
//
///** (B) 특정 플랫폼 월별 KPI 설명 */
//    public ExplainResponse explainPlatformMonthly(UserSession session, String platformCode, String dateRange) {
//        List<AdDataDTO.MonthlyTotalsRes> monthly = adDataService.reportByMonthAll(session, platformCode);
//        if (monthly == null || monthly.isEmpty()) throw new NotFoundException("해당 플랫폼의 월별 집계 데이터가 없습니다.");
//
//        ExplainRequest req = new ExplainRequest();
//        req.setAnonId(session.getAnonId());
//        req.setDateRange(dateRange == null ? "전체 업로드 범위" : dateRange);
//        req.setPlatformCode(platformCode);
//        req.setMonthly(
//                monthly.stream().map(m -> {
//                    ExplainRequest.MonthlyKpi row = new ExplainRequest.MonthlyKpi();
//                    row.setYear(m.getYear());
//                    row.setMonth(m.getMonth());
//                    row.setCpc(toD(m.getCpc()));      // 원
//                    row.setCvr(toD(m.getCvr()));      // 이미 % 스케일
//                    row.setRoas(toD(m.getRoas()));    // 이미 % 스케일
//                    row.setRoi(toD(m.getRoi()));      // 이미 % 스케일
//                    return row;
//                }).toList()
//        );
//
//        return kpiExplainService.explain(req);
//    }
//
//    private double toD(java.math.BigDecimal bd) { return bd == null ? 0d : bd.doubleValue(); }
//}
