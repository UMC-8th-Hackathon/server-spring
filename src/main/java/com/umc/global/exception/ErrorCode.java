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

    // 로그인 관련 에러
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "LOGIN_4001", "이미 존재하는 닉네임입니다. 비밀번호를 다시 입력해주세요."),
    LOGIN_NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "LOGIN_4002", "닉네임을 입력해주세요."),
    LOGIN_PASSWORD_EMPTY(HttpStatus.BAD_REQUEST, "LOGIN_4002", "비밀번호를 입력해주세요."),

    //토큰 관련 에러
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_4001", "토큰이 유효하지 않습니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "TOKEN_4002", "Authorization 헤더가 필요합니다."),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "TOKEN_4003", "토큰 형식이 올바르지 않습니다."),

    // 향수 관련 에러
    PERFUME_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFUME_4001", "해당 향수를 찾을 수 없습니다."),
    PERFUME_INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "PERFUME_4002", "perfumeId가 잘못된 형식입니다."),
    PERFUME_FILE_EMPTY(HttpStatus.BAD_REQUEST, "PERFUME_4003", "파일이 비어있습니다."),
    PERFUME_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "PERFUME_4004", "파일 크기가 제한을 초과했습니다."),
    PERFUME_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "PERFUME_4005", "지원하지 않는 파일 형식입니다."),
    PERFUME_INVALID_SOURCE_TYPE(HttpStatus.BAD_REQUEST, "PERFUME_4006", "잘못된 소스 타입입니다."),
    PERFUME_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PERFUME_5001", "향수 생성에 실패했습니다."),
    PERFUME_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PERFUME_4007", "해당 향수에 대한 접근 권한이 없습니다."),

    // 리뷰 관련 에러
    REVIEW_DESCRIPTION_EMPTY(HttpStatus.BAD_REQUEST, "REVIEW_4001", "리뷰 내용은 비어 있을 수 없습니다."),

    // 매장 관련 에러
    SHOP_INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "SHOP_4001", "잘못된 위도 또는 경도 값입니다."),
    SHOP_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SHOP_5001", "매장 검색에 실패했습니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_4001", "해당 사용자를 찾을 수 없습니다.");

    // TODO: 비즈니스 로직 개발하면서 필요한 에러코드들 추가

    private final HttpStatus status;
    private final String code;
    private final String message;
}