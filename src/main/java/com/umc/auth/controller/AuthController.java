package com.umc.auth.controller;

import com.umc.auth.dto.LoginRequest;
import com.umc.auth.dto.TokenResponse;
import com.umc.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByNickname(request.nickname())
                .orElseGet(() -> {
                    // 없으면 자동 회원가입
                    User newUser = new User();
                    newUser.setNickname(request.nickname());
                    newUser.setPassword(passwordEncoder.encode(request.password()));
                    return userRepository.save(newUser);
                });

        // 기존 유저인 경우 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // JWT 발급
        String token = jwtProvider.generateToken(user);
        return ResponseEntity.ok(new TokenResponse(token));
    }
}

