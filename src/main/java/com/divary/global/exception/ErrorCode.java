package com.divary.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_004", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_005", "접근이 거부되었습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_006", "요청한 리소스를 찾을 수 없습니다."),

    // Validation Errors  
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_001", "입력값 검증에 실패했습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "VALIDATION_002", "필수 필드가 누락되었습니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "VALIDATION_003", "잘못된 형식입니다."),

    // Business Logic Errors
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "BUSINESS_001", "중복된 리소스입니다."),
    OPERATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "BUSINESS_002", "허용되지 않는 작업입니다."),

    // Database Errors
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_001", "데이터베이스 오류가 발생했습니다."),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "DATABASE_002", "데이터베이스 제약 조건 위반입니다.");
    
    // TODO: 나중에 각 도메인 에러 추가

    private final HttpStatus status;
    private final String code;
    private final String message;
} 