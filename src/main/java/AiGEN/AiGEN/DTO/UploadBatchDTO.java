package AiGEN.AiGEN.DTO;

import AiGEN.AiGEN.domain.UploadBatch;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class UploadBatchDTO {

    /** 업로드 시작 요청 바디 */
    @Data
    public static class StartUploadReq {
        private String filename; // 예: "ad_report_2025-08-13.xlsx" 또는 ".csv"
    }

    /** 업로드 배치 응답 (간단 메타) */
    @Data
    @AllArgsConstructor
    public static class UploadBatchRes {
        private final Long id;
        private final String anonId;
        private final String filename;
        private final LocalDateTime uploadedAt;
    }

    /** 파싱 결과 응답 */
    @Data
    @AllArgsConstructor
    public static class ParseResultRes {
        private final Long batchId;
        private final int totalRows;
        private final int successRows;
        private final int failedRows;
        private final List<RowError> errors;
    }

    /** 실패 행 상세 */
    @Data
    @AllArgsConstructor
    public static class RowError {
        private final int rowIndex; // 헤더 다음부터 1 시작 권장
        private final String message;
    }
}
