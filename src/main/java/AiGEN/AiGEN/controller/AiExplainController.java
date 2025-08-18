package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.DTO.ExplainResponse;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.exception.InvalidHeaderException;
import AiGEN.AiGEN.service.ExplainAggregationService;
import AiGEN.AiGEN.service.KpiExplainService;
import AiGEN.AiGEN.service.UserSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/explain")
@RequiredArgsConstructor
public class AiExplainController {

    private final UserSessionService userSessionService;
    private final ExplainAggregationService explainAggregationService;

    @Operation(
            summary = "플랫폼별 합계 KPI 설명 생성",
            description = "현재 세션의 최신 배치 집계 결과를 바탕으로 각 플랫폼의 CPC, CVR, ROAS, ROI를 해설(JSON)로 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "설명 생성 성공",
                            content = @Content(schema = @Schema(implementation = ExplainResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "집계 데이터 없음"),
                    @ApiResponse(responseCode = "500", description = "외부 AI 호출 실패")
            }
    )
    @PostMapping(value = "/platforms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExplainResponse explainPlatforms(
            @RequestHeader("X-Anon-Id") String anonId) {
        validateAnonId(anonId);
        UserSession session = userSessionService.getOrCreate(anonId);
        return explainAggregationService.explainPlatformTotals(session);
    }

    @Operation(
            summary = "특정 플랫폼 월별 KPI 설명 생성",
            description = "현재 세션의 최신 배치 기준으로 특정 플랫폼의 월별 CPC, CVR, ROAS, ROI를 해설(JSON)로 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "설명 생성 성공",
                            content = @Content(schema = @Schema(implementation = ExplainResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "집계 데이터 없음"),
                    @ApiResponse(responseCode = "500", description = "외부 AI 호출 실패")
            }
    )
    @PostMapping(value = "/platforms/{platformCode}/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExplainResponse explainPlatformMonthly(
            @RequestHeader("X-Anon-Id") String anonId,
            @PathVariable String platformCode,
            @RequestParam(required = false, defaultValue = "전체 업로드 범위") String dateRange
    ) {
        validateAnonId(anonId);
        if (platformCode == null || platformCode.isBlank())
            throw new InvalidHeaderException("platformCode가 비어 있습니다.");
        UserSession session = userSessionService.getOrCreate(anonId);
        return explainAggregationService.explainPlatformMonthly(session, platformCode);
    }
    private void validateAnonId(String anonId) { UUID.fromString(anonId); }
}
