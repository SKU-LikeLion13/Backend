package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class UserSessionController {

    private final UserSessionService sessionService;

    @GetMapping("/me")
    public ResponseEntity<UserSession> getOrCreate(
            @RequestHeader(value = "X-Anon-Id", required = false) String anonId) {

        String valid = validateAnonId(anonId);
        return ResponseEntity.ok(sessionService.getOrCreate(valid));
    }

    @PostMapping("/me/touch")
    public ResponseEntity<UserSession> touch(
            @RequestHeader(value = "X-Anon-Id", required = false) String anonId) {

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
