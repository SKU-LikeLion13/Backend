package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.service.UserSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User Sessions", description = "익명 세션 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class UserSessionController {

    private final UserSessionService sessionService;

    @Operation(
            summary = "세션 조회 또는 생성",
            description = "클라이언트의 X-Anon-Id 헤더를 기반으로 현재 세션을 조회합니다. "
                    + "세션이 없으면 새로 생성하여 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "세션 정보 반환",
                            content = @Content(schema = @Schema(implementation = UserSession.class))),
                    @ApiResponse(responseCode = "400", description = "UUID 형식 오류 또는 헤더 누락")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserSession> getOrCreate(
            @RequestHeader(value = "X-Anon-Id") String anonId) {

        String valid = validateAnonId(anonId);
        return ResponseEntity.ok(sessionService.getOrCreate(valid));
    }

    @Operation(
            summary = "세션 최근 접속 시간 갱신",
            description = "기존 세션의 lastSeen 필드를 현재 시각으로 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "갱신된 세션 정보 반환",
                            content = @Content(schema = @Schema(implementation = UserSession.class))),
                    @ApiResponse(responseCode = "400", description = "UUID 형식 오류 또는 헤더 누락")
            }
    )

    @PostMapping("/me/touch")
    public ResponseEntity<UserSession> touch(
            @RequestHeader(value = "X-Anon-Id") String anonId) {

        String valid = validateAnonId(anonId);
        return ResponseEntity.ok(sessionService.touch(valid));
    }

    // ====== 내부 검증 ======
    private String validateAnonId(String anonId) {
        if (anonId == null || anonId.trim().isEmpty()) {
            throw new IllegalArgumentException("X-Anon-Id 헤더가 비어 있습니다.");
        }
        try {
            UUID.fromString(anonId); // 형식 검증
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("X-Anon-Id는 UUID 형식이어야 합니다.");
        }
        return anonId;
    }
}
