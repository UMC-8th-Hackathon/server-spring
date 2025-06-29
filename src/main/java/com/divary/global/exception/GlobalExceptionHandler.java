package com.divary.global.exception;

import com.divary.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    /**
     * @Valid 검증 실패 및 바인딩 실패 예외 처리
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    protected ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
        log.error("Validation Exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR));
    }

    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.REQUIRED_FIELD_MISSING));
    }

    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE));
    }

    /**
     * 지원하지 않는 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
    }

    /**
     * 요청한 리소스를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("NoHandlerFoundException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 접근 거부 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.ACCESS_DENIED));
    }

    /**
     * 데이터베이스 제약 조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("ConstraintViolationException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.CONSTRAINT_VIOLATION));
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}