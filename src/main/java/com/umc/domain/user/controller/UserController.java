package com.umc.domain.user.controller;

import com.umc.auth.util.JwtUtil;
import com.umc.common.response.ApiResponse;
import com.umc.domain.user.converter.UserConverter;
import com.umc.domain.user.dto.UserResponseDTO;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import com.umc.global.config.SwaggerConfig;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "유저 API", description = "유저 정보 관련 API")
@Slf4j
public class UserController {

    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    @Operation(summary = "자신의 닉네임 조회", description = "JWT 토큰을 기반으로 현재 유저의 닉네임을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @SwaggerConfig.ApiErrorExamples({
            ErrorCode.TOKEN_INVALID
    })
    public ResponseEntity<ApiResponse<UserResponseDTO.MyNameDTO>> getMyNickname(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        log.info("닉네임 조회 요청 - Authorization: {}", authorization);

        if (authorization == null || authorization.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Authorization 헤더가 없습니다.");
        }

        User user = jwtUtil.getUserFromHeader(authorization);
        UserResponseDTO.MyNameDTO response = UserConverter.toMyNameDTO(user);

        return ResponseEntity.ok(ApiResponse.success("닉네임 조회 성공", response));
    }
}



