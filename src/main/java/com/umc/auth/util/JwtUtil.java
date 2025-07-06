package com.umc.auth.util;

import com.umc.auth.Jwt.JwtProvider;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
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
        try {
            Long userId = getUserIdFromToken(token);
            return userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("토큰에서 추출한 사용자 ID로 사용자를 찾을 수 없습니다: {}", userId);
                        return new BusinessException(ErrorCode.USER_NOT_FOUND);
                    });
        } catch (BusinessException e) {
            throw e; // BusinessException은 그대로 전파
        } catch (Exception e) {
            log.warn("토큰 파싱 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * Authorization 헤더에서 사용자 정보를 조회합니다.
     */
    public User getUserFromHeader(String authorizationHeader) {
        // Authorization 헤더 존재 여부 검증
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            log.warn("Authorization 헤더가 없습니다.");
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }
        
        // Bearer 형식 검증
        if (!authorizationHeader.startsWith("Bearer ")) {
            log.warn("잘못된 Authorization 헤더 형식: {}", authorizationHeader);
            throw new BusinessException(ErrorCode.TOKEN_MALFORMED);
        }
        
        String token = extractTokenFromHeader(authorizationHeader);
        if (token == null || token.trim().isEmpty()) {
            log.warn("토큰 추출 실패 - Authorization: {}", authorizationHeader);
            throw new BusinessException(ErrorCode.TOKEN_MALFORMED);
        }
        
        return getUserFromToken(token);
    }
} 