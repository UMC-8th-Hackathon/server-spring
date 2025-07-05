package com.umc.auth.util;

import com.umc.auth.Jwt.JwtProvider;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    /**
     * Authorization 헤더에서 토큰을 추출합니다.
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     */
    public Long getUserIdFromToken(String token) {
        return jwtProvider.getUserId(token);
    }

    /**
     * 토큰에서 사용자 정보를 조회합니다.
     */
    public User getUserFromToken(String token) {
        Long userId = getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
    }

    /**
     * Authorization 헤더에서 사용자 정보를 조회합니다.
     */
    public User getUserFromHeader(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        if (token == null) {
            throw new RuntimeException("유효한 토큰이 없습니다.");
        }
        return getUserFromToken(token);
    }
} 