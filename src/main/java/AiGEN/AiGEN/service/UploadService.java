package AiGEN.AiGEN.service;

import AiGEN.AiGEN.DTO.UploadBatchDTO;
import AiGEN.AiGEN.domain.AdData;
import AiGEN.AiGEN.domain.AdPlatform;
import AiGEN.AiGEN.domain.UploadBatch;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.exception.BadRequestException;
import AiGEN.AiGEN.exception.NotFoundException;
import AiGEN.AiGEN.repository.AdDataRepo;
import AiGEN.AiGEN.repository.AdPlatformRepo;
import AiGEN.AiGEN.repository.UploadBatchRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UploadService {
    private final UploadBatchRepo uploadBatchRepo;
    private final UserSessionService userSessionService;
    private final AdPlatformRepo adPlatformRepo;
    private final AdDataRepo adDataRepo;

    /** 업로드 시작: 배치 메타 생성 */
    @Transactional
    public UploadBatch startUpload(String anonId, String filename) {
        UserSession session = userSessionService.getOrCreate(anonId);
        UploadBatch batch = new UploadBatch(session, filename, LocalDateTime.now());
        return uploadBatchRepo.save(batch);
    }

    /** 최근 업로드 파일명만 반환 */
    @Transactional(readOnly = true)
    public List<String> listFilenames(String anonId, int limit) {
        return uploadBatchRepo.findAllFilenames(anonId, limit).stream()
                .filter(fn -> fn != null && !fn.isBlank())
                .distinct()
                .toList();
    }

    @Transactional
    public UploadBatchDTO.ParseResultRes parseDirect(String anonId, MultipartFile file) {
        // 1) 업로드된 원본 파일명 확보 (null/공백 방어)
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse("uploaded_" + java.time.LocalDateTime.now());

        // 2) 배치 생성(파일명 저장)
        UploadBatch batch = startUpload(anonId, filename);

        // 3) 즉시 파싱 + 저장 (기존 로직 재사용)
        return parseAndSave(batch.getId(), anonId, file);
    }
/*숫자파싱 헬퍼*/
    private long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return 0L;
        // 천단위 콤마/공백 제거
        String cleaned = s.replace(",", "").replace(" ", "");
        // 소수점이 있으면 반올림
        if (cleaned.contains(".")) {
            return Math.round(Double.parseDouble(cleaned));
        }
        return Long.parseLong(cleaned);
    }
    /** CSV/XLSX 파싱 → AdData 저장 */
    @Transactional
    public UploadBatchDTO.ParseResultRes parseAndSave(Long batchId, String anonId, MultipartFile file) {
        UploadBatch batch = uploadBatchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("UploadBatch not found: " + batchId));

        // 세션 보안: 헤더 anonId와 배치 소유자 일치 체크(선택)
        if (!batch.getUserSession().getAnonId().equals(anonId)) {
            throw new IllegalArgumentException("Batch owner mismatch");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean isCsv = name.endsWith(".csv");
        boolean isXlsx = name.endsWith(".xlsx");

        List<UploadBatchDTO.RowError> errors = new ArrayList<>();
        int total = 0;
        int success = 0;

        try {
            if (isCsv) {
                // CSV: 헤더 1행 가정: platform,date,cost,clicks,conversions,revenue
                try (var br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    int rowIndex = 0;
                    while ((line = br.readLine()) != null) {
                        rowIndex++;
                        if (rowIndex == 1) continue; // 헤더 스킵
                        if (line.isBlank()) continue;
                        total++;

                        String[] cols = line.split(",", -1);
                        // 최소 6컬럼 체크
                        if (cols.length < 6) {
                            errors.add(new UploadBatchDTO.RowError(rowIndex - 1, "컬럼 수 부족"));
                            continue;
                        }
                        try {
                            String platformCode = cols[0].trim();
                            LocalDate date = LocalDate.parse(cols[1].trim());
                            long cost = Long.parseLong(cols[2].trim());
                            int clicks = Integer.parseInt(cols[3].trim());
                            int conv = Integer.parseInt(cols[4].trim());
                            long revenue = Long.parseLong(cols[5].trim());

                            AdPlatform platformRef = adPlatformRepo.ensureAndGetRef(platformCode);
                            UserSession sessionRef = batch.getUserSession();

                            AdData ad = new AdData(batch, sessionRef, platformRef, date, cost, clicks, conv, revenue);
                            adDataRepo.save(ad);
                            success++;
                        } catch (Exception e) {
                            errors.add(new UploadBatchDTO.RowError(rowIndex - 1, "파싱 실패: " + e.getMessage()));
                        }
                    }
                }
                // ===== XLSX 안전 파싱 (셀 타입 무관) =====
            } else if (isXlsx) {
                try (var wb = new XSSFWorkbook(file.getInputStream())) {
                    Sheet sheet = wb.getSheetAt(0);
                    int firstRow = sheet.getFirstRowNum();
                    int lastRow  = sheet.getLastRowNum();
                    var fmt = new org.apache.poi.ss.usermodel.DataFormatter();

                    for (int i = firstRow + 1; i <= lastRow; i++) { // 헤더 스킵
                        Row r = sheet.getRow(i);
                        if (r == null) continue;
                        total++;

                        try {
                            // 0: platform (문자/숫자/혼합 상관없이 화면 표시 그대로)
                            String platformCode = fmt.formatCellValue(r.getCell(0)).trim();

                            // 1: date (엑셀 날짜/문자 모두 대응)
                            var cDate = r.getCell(1);
                            java.time.LocalDate date;
                            if (cDate != null
                                    && cDate.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                                    && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cDate)) {
                                // 엑셀 진짜 날짜 셀
                                date = cDate.getLocalDateTimeCellValue().toLocalDate();
                            } else {
                                // 문자열/혼합형: DataFormatter로 보이는 값 → LocalDate 파싱
                                String dateStr = fmt.formatCellValue(cDate).trim(); // 예: 2025-08-11
                                if (dateStr.isEmpty()) {
                                    throw new IllegalArgumentException("date empty");
                                }
                                date = java.time.LocalDate.parse(dateStr);
                            }

                            // 2~5: 숫자 컬럼 (숫자/문자 모두 대응)
                            long cost   = parseLongSafe(fmt.formatCellValue(r.getCell(2)).trim());
                            int clicks  = (int) parseLongSafe(fmt.formatCellValue(r.getCell(3)).trim());
                            int conv    = (int) parseLongSafe(fmt.formatCellValue(r.getCell(4)).trim());
                            long revenue= parseLongSafe(fmt.formatCellValue(r.getCell(5)).trim());

                            AdPlatform platformRef = adPlatformRepo.ensureAndGetRef(platformCode);
                            UserSession sessionRef = batch.getUserSession();

                            AdData ad = new AdData(batch, sessionRef, platformRef, date, cost, clicks, conv, revenue);
                            adDataRepo.save(ad);
                            success++;

                        } catch (Exception e) {
                            errors.add(new UploadBatchDTO.RowError(
                                    i /* 헤더 제외, 시트 실제 row index */, "파싱 실패: " + e.getMessage()));
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. csv 또는 xlsx 만 지원");
            }
        } catch (Exception ex) {
            // 전체 실패 케이스
            errors.add(new UploadBatchDTO.RowError(0, "파일 처리 중 오류: " + ex.getMessage()));
        }

        int failed = total - success;
        return new UploadBatchDTO.ParseResultRes(batchId, total, success, failed, errors);
    }
}
