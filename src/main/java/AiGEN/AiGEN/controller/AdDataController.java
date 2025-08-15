package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.DTO.AdDataDTO;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.service.AdDataService;
import AiGEN.AiGEN.service.UserSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "Ad Data Reports", description = "광고데이터 리포트(플랫폼 합계/월별 합계)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ad-data")
public class AdDataController {
    private final AdDataService adDataService;
    private final UserSessionService userSessionService;

    @Operation(
            summary = "플랫폼별 합계/지표(전체 범위)",
            description = "업로드된 전체 데이터 기준으로 각 플랫폼의 CPC, CVR, ROAS, ROI를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "플랫폼별 합계",
                            content = @Content(array = @ArraySchema(schema =
                            @Schema(implementation = AdDataDTO.PlatformTotalsRes.class))))
            }
    )
    @GetMapping("/reports/platforms")
    public List<AdDataDTO.PlatformTotalsRes> reportByPlatformAll(
            @RequestHeader("X-Anon-Id") String anonId
    ) {
        validateAnonId(anonId);
        UserSession session = userSessionService.getOrCreate(anonId);
        return adDataService.reportByPlatformAll(session);
    }

    @Operation(
            summary = "특정 플랫폼의 월별 합계/지표(전체 범위)",
            description = "업로드된 전체 데이터 기준으로 특정 플랫폼의 월별 CPC, CVR, ROAS, ROI를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "월별 합계",
                            content = @Content(array = @ArraySchema(schema =
                            @Schema(implementation = AdDataDTO.MonthlyTotalsRes.class))))
            }
    )
    @GetMapping("/reports/{platformCode}/monthly")
    public List<AdDataDTO.MonthlyTotalsRes> reportByMonthAll(
            @RequestHeader("X-Anon-Id") String anonId,
            @PathVariable String platformCode
    ) {
        validateAnonId(anonId);
        UserSession session = userSessionService.getOrCreate(anonId);
        return adDataService.reportByMonthAll(session, platformCode);
    }

    @Operation(
            summary = "현재 세션의 플랫폼 목록",
            description = "업로드된 데이터에 포함된 플랫폼 코드와 이름 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "플랫폼 목록",
                            content = @Content(array = @ArraySchema(schema =
                            @Schema(implementation = AdDataDTO.PlatformInfoRes.class))))
            }
    )
    @GetMapping("/platforms")
    public List<AdDataDTO.PlatformInfoRes> listPlatforms(
            @RequestHeader("X-Anon-Id") String anonId
    ) {
        validateAnonId(anonId);
        UserSession session = userSessionService.getOrCreate(anonId);
        return adDataService.getPlatforms(session);
    }

    private void validateAnonId(String anonId) { UUID.fromString(anonId); }
}
