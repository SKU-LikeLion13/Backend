package AiGEN.AiGEN.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<String, Object> body(HttpServletRequest req, int status, String code, String msg) {
        return Map.of(
                "status", status,
                "code", code,
                "message", msg,
                "path", req.getRequestURI(),
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String,Object>> badRequest(HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                body(req, 400, "BAD_REQUEST", "잘못된 요청입니다. 입력값을 확인하세요.")
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String,Object>> notFound(HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                body(req, 404, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.")
        );
    }

    // 스프링/DB 기본 예외 매핑
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String,Object>> missingHeader(HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                body(req, 400, "MISSING_HEADER", "필수 요청 헤더가 누락되었습니다.")
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,Object>> invalidJson(HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                body(req, 400, "INVALID_JSON", "요청 본문(JSON)을 읽을 수 없습니다.")
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> integrity(HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                body(req, 409, "DATA_INTEGRITY", "데이터 무결성 제약을 위반했습니다.")
        );
    }

    // 안전망
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> illegalArg(HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                body(req, 400, "BAD_REQUEST", "요청 파라미터가 올바르지 않습니다.")
        );
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String,Object>> unknown(HttpServletRequest req) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                body(req, 500, "INTERNAL_ERROR", "예상치 못한 서버 오류가 발생했습니다.")
//        );
//    }
    @ExceptionHandler(InvalidHeaderException.class)
    public ResponseEntity<Map<String,Object>> invalidHeader(HttpServletRequest req, InvalidHeaderException ex) {
        return ResponseEntity.badRequest().body(
                body(req, 400, "INVALID_HEADER", ex.getMessage())
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> unknown(HttpServletRequest req, Exception e) {
        log.error("[INTERNAL_ERROR] {} {}", req.getMethod(), req.getRequestURI(), e); // ★ 스택 로그
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                body(req, 500, "INTERNAL_ERROR", "예상치 못한 서버 오류가 발생했습니다.")
        );
    }
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<Map<String, Object>> handle429(HttpClientErrorException.TooManyRequests e) {
        String body = e.getResponseBodyAsString();
        HttpHeaders h = e.getResponseHeaders();
        log.error("[VEO-429] body={}, headers={}", body, h);

        Map<String,Object> out = new HashMap<>();
        out.put("code", "RATE_LIMITED");
        out.put("status", 429);
        out.put("message", "Vertex AI is busy. Please retry with backoff.");
        out.put("serverTimeKST", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());
        out.put("serverTimeUTC", ZonedDateTime.now(ZoneOffset.UTC).toString());
        return ResponseEntity.status(429).body(out);
    }
    private static String firstNonNull(String... v) { for (String s : v) if (s != null) return s; return null; }
}
