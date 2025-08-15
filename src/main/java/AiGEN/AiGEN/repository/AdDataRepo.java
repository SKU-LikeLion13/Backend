package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.AdData;
import AiGEN.AiGEN.domain.AdPlatform;
import AiGEN.AiGEN.domain.UploadBatch;
import AiGEN.AiGEN.domain.UserSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdDataRepo {

    AdData save(AdData adData);

    // 최신 배치 + 세션 범위로 플랫폼 합계/지표
    List<Object[]> sumByPlatformForBatchOfSession(UserSession session, Long batchId);

    // 최신 배치 + 세션 범위로 특정 플랫폼 월별 합계/지표
    List<Object[]> sumByMonthForBatchAndPlatformOfSession(UserSession session, Long batchId, String platformCode);

    // 플랫폼 목록 (세션 스코프)
    List<AdPlatform> findDistinctPlatforms(UserSession session,Long batchId);
}
