package AiGEN.AiGEN.service;

import AiGEN.AiGEN.DTO.AdDataDTO;
import AiGEN.AiGEN.DTO.ExplainResponse;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.exception.NotFoundException;
import AiGEN.AiGEN.repository.UploadBatchRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExplainAggregationService {
    /*집계 → 내부요청 변환*/

    private final AdDataService adDataService;
    private final KpiExplainService kpiExplainService;
    private final UploadBatchRepo uploadBatchRepo;
    
    /** (A) 플랫폼별 합계 KPI 설명 */
    public ExplainResponse explainPlatformTotals(UserSession session) {
        Long latestBatchId = uploadBatchRepo.findLatestIdBySession(session.getAnonId())
                .orElseThrow(() -> new IllegalArgumentException("최근 업로드 배치가 없습니다."));
        List<AdDataDTO.PlatformTotalsRes> totals = adDataService.reportByPlatformAll(session);
        if (totals == null || totals.isEmpty())
            throw new NotFoundException("설명을 생성할 집계 데이터가 없습니다.");

        String anonId = session.getAnonId();
        String safeRange = "전체 업로드 범위";

        StringBuilder prompt = new StringBuilder();
        prompt.append("아래는 광고 플랫폼별 집계 KPI입니다. 기간: ").append(safeRange).append("\n")
                .append("- 지표 단위: 금액(원), 비율(%)\n");

        for (AdDataDTO.PlatformTotalsRes t : totals) {
            prompt.append(String.format(
                    "플랫폼=%s | 비용=%d원 | 클릭수=%d | 전환수=%d | 매출=%d원 | CPC=%.2f원 | CVR=%.2f%% | ROAS=%.2f%% | ROI=%.2f%%%n",
                    t.getPlatformCode(), t.getCostSum(), t.getClicksSum(), t.getConvSum(), t.getRevenueSum(),
                    toD(t.getCpc()), toD(t.getCvr()), toD(t.getRoas()), toD(t.getRoi())
            ));
        }
        prompt.append(
                "\n다음 지침을 반드시 따르라:\n" +
                        "1. 결과는 반드시 아래 JSON 스키마 형식을 정확히 준수하라.\n" +
                        "   절대 코드블록(\\`\\`\\`),(````), 마크다운, 불필요한 설명 텍스트를 포함하지 마라.\n" +
                        "   {\n" +
                        "     \"bullets\": [string],            // 핵심 성과 인사이트 (정량/정성 지표 요약)\n" +
                        "     \"risks\": [string],              // 데이터 해석 시 고려해야 할 리스크나 한계점\n" +
                        "     \"nextActions\": [string]         // 후속 실행 전략 또는 개선 권고 사항\n" +
                        "   }\n\n" +
                        "2. 각 필드는 반드시 최소 1개 이상의 의미 있는 항목을 포함해야 하며, 빈 배열([])은 절대 허용되지 않는다.\n" +
                        "3. 작성 톤은 전문 컨설턴트가 경영진에게 보고하는 수준의 분석 보고서 스타일로 하라.\n" +
                        "   - bullets: 주어진 CPC, CVR, ROAS, ROI의 수치와 추세(상승/하락/안정)를 해석한 핵심 분석을 3~5줄 작성한다.\n" +
                        "   - risks:  데이터 부족, 외부 요인(경쟁사, 계절성 등) 같은 일반적 한계 설명은 절대 넣지 말고, **숫자 변화에서 직접 파생되는 위험 신호**만 기술한다.\n" +
                        "   - nextActions: 각 위험 신호나 지표 추세에 대응할 **구체적인 실행 전략**만 작성한다. (예: 예산 재분배, 키워드/소재 최적화, 타겟 재설정)\n" +
                        "4. 모든 수치는 반드시 입력 데이터에 기반해 설명하되, 맥락과 의미를 전문가 관점에서 이해하기 쉽게 해석하라. 그리고 어떤 플랫폼을 추천하고 비추천하는지 그 이유도 설명.\n"
        );

        return kpiExplainService.explain(prompt.toString(), anonId, latestBatchId);
    }

    /** (B) 특정 플랫폼 월별 KPI 설명 */
    public ExplainResponse explainPlatformMonthly(UserSession session, String platformCode) {
        Long latestBatchId = uploadBatchRepo.findLatestIdBySession(session.getAnonId())
                .orElseThrow(() -> new IllegalArgumentException("최근 업로드 배치가 없습니다."));
        List<AdDataDTO.MonthlyTotalsRes> monthly = adDataService.reportByMonthAll(session, platformCode);
        if (monthly == null || monthly.isEmpty())
            throw new NotFoundException("해당 플랫폼의 월별 집계 데이터가 없습니다.");

        String anonId = session.getAnonId();

        StringBuilder prompt = new StringBuilder();
        prompt.append("아래는 특정 플랫폼의 월별 KPI입니다. 플랫폼: ").append(platformCode)
                .append("- 지표 단위: 금액(원), 비율(%)\n");

        for (AdDataDTO.MonthlyTotalsRes m : monthly) {
            prompt.append(String.format(
                    "%d-%02d | 비용=%d원 | 클릭수=%d | 전환수=%d | 매출=%d원 | CPC=%.2f원 | CVR=%.2f%% | ROAS=%.2f%% | ROI=%.2f%%%n",
                    m.getYear(), m.getMonth(), m.getCostSum(), m.getClicksSum(), m.getConvSum(), m.getRevenueSum(),
                    toD(m.getCpc()), toD(m.getCvr()), toD(m.getRoas()), toD(m.getRoi())
            ));
        }
        prompt.append(
                "\n다음 지침을 반드시 따르라:\n" +
                        "1. 결과는 반드시 아래 JSON 스키마 형식을 정확히 준수하라.\n" +
                        "   절대 코드블록(\\`\\`\\`),(````), 마크다운, 불필요한 설명 텍스트를 포함하지 마라.\n" +
                        "   {\n" +
                        "     \"bullets\": [string],            // 핵심 성과 인사이트 (정량/정성 지표 요약)\n" +
                        "     \"risks\": [string],              // 데이터 해석 시 고려해야 할 리스크나 한계점\n" +
                        "     \"nextActions\": [string]         // 후속 실행 전략 또는 개선 권고 사항\n" +
                        "   }\n\n" +
                        "2. 각 필드는 반드시 최소 1개 이상의 의미 있는 항목을 포함해야 하며, 빈 배열([])은 절대 허용되지 않는다.\n" +
                        "3. 작성 톤은 전문 컨설턴트가 경영진에게 보고하는 수준의 분석 보고서 스타일로 하라.\n" +
                        "   - bullets: 주어진 CPC, CVR, ROAS, ROI의 수치와 추세(상승/하락/안정)를 해석한 핵심 분석을 3~5줄 작성한다.\n" +
                        "   - risks:  데이터 부족, 외부 요인(경쟁사, 계절성 등) 같은 일반적 한계 설명은 절대 넣지 말고, **숫자 변화에서 직접 파생되는 위험 신호**만 기술한다.\n" +
                        "   - nextActions: 각 위험 신호나 지표 추세에 대응할 **구체적인 실행 전략**만 작성한다. (예: 예산 재분배, 키워드/소재 최적화, 타겟 재설정)\n" +
                        "4. 모든 수치는 반드시 입력 데이터에 기반해 설명하되, 맥락과 의미를 전문가 관점에서 해석하라.\n"
        );
        return kpiExplainService.explain(prompt.toString(), anonId,latestBatchId);
    }

    private double toD(java.math.BigDecimal bd) {
        return bd == null ? 0d : bd.doubleValue();
    }
}