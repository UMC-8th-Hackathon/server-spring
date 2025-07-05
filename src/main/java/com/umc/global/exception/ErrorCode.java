package com.umc.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 실제 사용되는 공통 에러들
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),

    // 실제 사용되는 검증 에러들
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_001", "입력값 검증에 실패했습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "VALIDATION_002", "필수 필드가 누락되었습니다."),

    // 토큰 관련 에러
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_4001", "유효하지 않은 토큰입니다."),

    // 로그인 관련 에러
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "LOGIN_4001", "이미 존재하는 닉네임입니다. 비밀번호를 다시 입력해주세요."),

    // 향수 관련 에러
    PERFUME_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFUME_4001", "해당 향수를 찾을 수 없습니다.");

    // TODO: 비즈니스 로직 개발하면서 필요한 에러코드들 추가

    private final HttpStatus status;
    private final String code;
    private final String message;
}