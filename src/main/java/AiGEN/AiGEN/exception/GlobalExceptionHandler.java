package AiGEN.AiGEN.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> unknown(HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                body(req, 500, "INTERNAL_ERROR", "예상치 못한 서버 오류가 발생했습니다.")
        );
    }
}
