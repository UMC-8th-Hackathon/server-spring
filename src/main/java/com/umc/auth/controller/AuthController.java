package com.umc.auth.controller;

import com.umc.auth.Jwt.JwtProvider;
import com.umc.auth.dto.LoginRequest;
import com.umc.auth.dto.TokenResponse;
import com.umc.common.response.ApiResponse;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Operation(summary = "로그인 (회원가입 없음)", description = "닉네임 + 비밀번호로 로그인 요청. 닉네임이 존재하지 않으면 자동으로 유저 생성 후 로그인합니다. 기존에 등록된 닉네임인 경우 비밀번호 검증 후 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByNickname(request.nickname())
                .orElseGet(() -> {
                    // 없으면 자동 회원가입
                    User newUser = User.builder()
                            .nickname(request.nickname())
                            .password(passwordEncoder.encode(request.password()))
                            .build();
                    return userRepository.save(newUser);
                });

        // 기존 유저인 경우 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // JWT 발급
        String token = jwtProvider.generateToken(user);
        TokenResponse response = new TokenResponse(user.getId(), user.getNickname(), token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

