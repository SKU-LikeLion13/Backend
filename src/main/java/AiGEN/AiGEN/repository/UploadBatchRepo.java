package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.UploadBatch;
import AiGEN.AiGEN.domain.UserSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UploadBatchRepo {
    /** 신규/수정 저장 (ID null이면 persist, 아니면 merge) */
    UploadBatch save(UploadBatch entity);

    /** PK 조회 */
    Optional<UploadBatch> findById(Long id);

    /** 특정 세션의 모든 배치(최신순) */
    List<UploadBatch> findBySession(UserSession session);

    /** 특정 사용자(anonId)의 파일명 목록 (최신순, 최대 limit건) */
    List<String> findAllFilenames(String anonId, int limit);

    /**  가장 최근 배치 1건 */
    Optional<Long> findLatestIdBySession(String anonId);

//    /** 특정 사용자(anonId)의 기간 내 배치(업로드 시각 기준, 최신순) */
//    List<UploadBatch> findBySessionAndRange(String anonId, LocalDateTime from, LocalDateTime to);
//
//    /** 특정 사용자(anonId)의 배치 일괄 삭제 (필요 시 사용) */
//    int deleteBySession(String anonId);
}
