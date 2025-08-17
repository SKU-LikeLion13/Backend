//package AiGEN.AiGEN.service;
//
//import AiGEN.AiGEN.DTO.ExplainResponse;
//import AiGEN.AiGEN.domain.AiExplainLog;
//import AiGEN.AiGEN.repository.AiExplainLogRepo;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class KpiExplainService {
//    private final OpenRouterClient openRouterClient;
//    private final AiExplainLogRepo logRepo;
//    private final ObjectMapper om = new ObjectMapper();
//
//    public ExplainResponse explain(ExplainRequest req) {
//        String inputJson = toJson(req);
//
//        // PENDING 로그 저장 (createdAt은 @PrePersist로 자동 세팅)
//        AiExplainLog saved = logRepo.save(AiExplainLog.pending(req.getAnonId(), req.getBatchId(), inputJson));
//
//        OpenRouterClient.Message sys = new OpenRouterClient.Message("system", systemPrompt());
//        OpenRouterClient.Message user = new OpenRouterClient.Message("user", userPrompt(req));
//
//        String raw = null;
//        try {
//            raw = openRouterClient.chat(List.of(sys, user));
//            ExplainResponse out = parseOrWrap(raw);
//            logRepo.updateStatusAndOutput(saved.getId(), "OK", toJson(out));
//            return out;
//
//        } catch (Exception ex) {
//            // 실패 기록 후 예외 전파(→ GlobalExceptionHandler에서 500으로 일관 처리)
//            String errOut = toJson(new ExplainResponse(
//                    "설명 생성 실패",
//                    List.of("모델 호출 오류", ex.getMessage()),
//                    List.of(),
//                    List.of("잠시 후 재시도", "모델 교체 또는 타임아웃 연장"),
//                    raw == null ? "" : raw
//            ));
//            logRepo.updateStatusAndOutput(saved.getId(), "ERROR", errOut);
//            throw new IllegalStateException("외부 AI 호출 실패: " + ex.getMessage(), ex);
//        }
//    }
//
//    private String systemPrompt() {
//        return """
//            너는 대한민국 마케팅 데이터 분석가이자 카피라이터야.
//            사용자가 바로 실행할 수 있게 간결·실무형 한국어로 설명해줘.
//            오직 JSON만 반환해:
//            {
//              "headline": string,
//              "bullets": string[],      // 3~5줄 핵심
//              "risks": string[],        // 2~4줄 리스크/주의
//              "nextActions": string[]   // 2~4줄 다음 액션 (구체적으로)
//            }
//            숫자에는 단위를 붙여(원, %, 배). 비교/추이/의미를 짚고, 결정 포인트를 분명히 해.
//        """;
//    }
//
//    private String userPrompt(ExplainRequest req) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("기간: ").append(req.getDateRange() == null ? "전체 업로드 범위" : req.getDateRange()).append("\n");
//
//        if (req.getPlatforms() != null && !req.getPlatforms().isEmpty()) {
//            sb.append("[모드: 플랫폼별 합계 KPI]\n지표 정의: CPC(원), CVR(%), ROAS(%), ROI(%)\n");
//            for (var p : req.getPlatforms()) {
//                sb.append("- ").append(p.getPlatformCode()).append(": ")
//                        .append("CPC ").append(fmt(p.getCpc())).append("원, ")
//                        .append("CVR ").append(fmt(p.getCvr())).append("%, ")
//                        .append("ROAS ").append(fmt(p.getRoas())).append("%, ")
//                        .append("ROI ").append(fmt(p.getRoi())).append("%\n");
//            }
//            sb.append("질문: 어떤 플랫폼이 효율적이며, 어떤 최적화를 바로 하면 좋은가?\n");
//        } else if (req.getMonthly() != null && !req.getMonthly().isEmpty()) {
//            sb.append("[모드: 특정 플랫폼 월별 KPI]\n플랫폼: ").append(req.getPlatformCode())
//                    .append("\n지표 정의: CPC(원), CVR(%), ROAS(%), ROI(%)\n");
//            for (var m : req.getMonthly()) {
//                sb.append("- ").append(m.getYear()).append("-").append(String.format("%02d", m.getMonth())).append(": ")
//                        .append("CPC ").append(fmt(m.getCpc())).append("원, ")
//                        .append("CVR ").append(fmt(m.getCvr())).append("%, ")
//                        .append("ROAS ").append(fmt(m.getRoas())).append("%, ")
//                        .append("ROI ").append(fmt(m.getRoi())).append("%\n");
//            }
//            sb.append("질문: 추세상 문제가 되는 시점과 원인 가설, 다음 액션은?\n");
//        } else {
//            sb.append("입력 KPI가 비어있음. 에러 JSON으로 응답해.\n");
//        }
//
//        sb.append("응답은 반드시 위 JSON 스키마로만 출력.");
//        return sb.toString();
//    }
//
//    private String toJson(Object o) { try { return om.writeValueAsString(o); } catch (Exception e) { return "{}"; } }
//    private ExplainResponse parseOrWrap(String raw) {
//        try {
//            ExplainResponse r = om.readValue(raw, ExplainResponse.class);
//            r.setRawModelOutput(raw);
//            return r;
//        } catch (Exception e) {
//            return new ExplainResponse("요약",
//                    List.of(raw != null && raw.length() > 400 ? raw.substring(0, 400) + "..." : String.valueOf(raw)),
//                    new ArrayList<>(), new ArrayList<>(), raw);
//        }
//    }
//    private String fmt(double v) { return String.format("%.2f", v); }
//}
