package AiGEN.AiGEN.service;

import AiGEN.AiGEN.DTO.AdDataDTO;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.repository.AdDataRepo;
import AiGEN.AiGEN.repository.UploadBatchRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdDataService {
    private final AdDataRepo adDataRepo;
    private final UploadBatchRepo uploadBatchRepo;

    @Transactional(readOnly = true)
    public List<AdDataDTO.PlatformTotalsRes> reportByPlatformAll(UserSession session) {
        Long batchId = uploadBatchRepo.findLatestIdBySession(session.getAnonId())
                .orElseThrow(() -> new IllegalArgumentException("최근 업로드 배치가 없습니다."));
        List<Object[]> rows = adDataRepo.sumByPlatformForBatchOfSession(session, batchId);
        // ↓ 기존 변환 로직 그대로
        return rows.stream().map(r -> {
            String code  = (String)  r[0];
            long cost    = ((Number) r[1]).longValue();
            int clicks   = ((Number) r[2]).intValue();
            int conv     = ((Number) r[3]).intValue();
            long revenue = ((Number) r[4]).longValue();
            var cpc  = div(cost, clicks);
            var cvr  = div(conv, clicks);
            var roas = div(revenue, cost);
            var roi  = cost == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(revenue - cost)
                    .divide(BigDecimal.valueOf(cost), 4, RoundingMode.HALF_UP);
            return new AdDataDTO.PlatformTotalsRes(code, cost, clicks, conv, revenue, cpc, cvr, roas, roi);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdDataDTO.MonthlyTotalsRes> reportByMonthAll(UserSession session, String platformCode) {
        Long batchId = uploadBatchRepo.findLatestIdBySession(session.getAnonId())
                .orElseThrow(() -> new IllegalArgumentException("최근 업로드 배치가 없습니다."));
        List<Object[]> rows = adDataRepo.sumByMonthForBatchAndPlatformOfSession(session, batchId, platformCode);
        // ↓ 기존 변환 로직 그대로
        return rows.stream().map(r -> {
            int year     = ((Number) r[0]).intValue();
            int month    = ((Number) r[1]).intValue();
            long cost    = ((Number) r[2]).longValue();
            int clicks   = ((Number) r[3]).intValue();
            int conv     = ((Number) r[4]).intValue();
            long revenue = ((Number) r[5]).longValue();
            var cpc  = div(cost, clicks);
            var cvr  = div(conv, clicks);
            var roas = div(revenue, cost);
            var roi  = cost == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(revenue - cost)
                    .divide(BigDecimal.valueOf(cost), 4, RoundingMode.HALF_UP);
            return new AdDataDTO.MonthlyTotalsRes(year, month, cost, clicks, conv, revenue, cpc, cvr, roas, roi);
        }).collect(Collectors.toList());
    }

    private BigDecimal div(long n, long d) {
        if (d == 0L) return BigDecimal.ZERO;
        return BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d), 4, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<AdDataDTO.PlatformInfoRes> getPlatforms(UserSession session) {
        Long latestBatchId = uploadBatchRepo.findLatestIdBySession(session.getAnonId())
                .orElseThrow(() -> new IllegalArgumentException("최근 업로드 배치가 없습니다."));
        return adDataRepo.findDistinctPlatforms(session, latestBatchId).stream()
                .map(p -> new AdDataDTO.PlatformInfoRes(p.getCode(), p.getName()))
                .toList();
    }
}
