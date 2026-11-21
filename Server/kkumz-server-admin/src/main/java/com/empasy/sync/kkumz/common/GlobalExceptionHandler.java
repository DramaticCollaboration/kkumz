package com.empasy.sync.kkumz.common;

import com.empasy.sync.core.i18n.SyncMessageResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("요청 경로를 찾을 수 없음 (404): {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(404, SyncMessageResolver.resolve("common.not.found") + ": /" + e.getResourcePath()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Result<Void>> handleSecurityException(SecurityException e) {
        String msg = e.getMessage() != null ? e.getMessage() : SyncMessageResolver.resolve("auth.forbidden");
        if (msg.startsWith("[401]")) {
            String cleanMsg = msg.substring(5).trim();
            log.warn("인증 필요 요청 차단 (401): {}", cleanMsg);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, cleanMsg.isEmpty() ? SyncMessageResolver.resolve("auth.login.required") : cleanMsg));
        }
        String cleanMsg = msg.startsWith("[403]") ? msg.substring(5).trim() : msg;
        log.warn("권한 없는 리소스 수정 차단 (403): {}", cleanMsg);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, cleanMsg.isEmpty() ? SyncMessageResolver.resolve("auth.forbidden") : cleanMsg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse(SyncMessageResolver.resolve("common.param.invalid"));
        log.warn("요청 유효성 검증 실패: {}", detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, detail));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청 파라미터: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, e.getMessage() != null ? e.getMessage() : SyncMessageResolver.resolve("common.param.invalid")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("서버 처리 중 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, SyncMessageResolver.resolve("common.internal.error")));
    }
}
