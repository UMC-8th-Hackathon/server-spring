package com.umc.domain.perfume.controller;

import com.umc.auth.util.JwtUtil;
import com.umc.common.response.ApiResponse;
import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.service.PerfumeService;
import java.util.List;
import com.umc.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "향수 API", description = "향수 생성 및 조회 API")
public class PerfumeController {

    private final PerfumeService perfumeService;
    private final JwtUtil jwtUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "향수 생성",
        description = "오디오 또는 이미지 파일을 업로드하여 향수 정보를 생성합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "향수 생성 성공",
            content = @Content(schema = @Schema(implementation = PerfumeResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (파일 형식 오류, 크기 초과 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패 (JWT 토큰 오류)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 오류"
        )
    })
    public ApiResponse<PerfumeResponseDto> createPerfume(
            @Parameter(description = "소스 타입 (AUDIO 또는 IMAGE)", required = true)
            @RequestParam("sourceType") SourceType sourceType,
            
            @Parameter(description = "업로드할 파일 (오디오 또는 이미지)", required = true)
            @RequestParam("file") MultipartFile file,
            
            HttpServletRequest request) {
        
        try {
            // Authorization 헤더 추출 및 검증
            String authorization = request.getHeader("Authorization");
            log.info("향수 생성 요청 - sourceType: {}, fileName: {}, fileSize: {}MB", 
                    sourceType, 
                    file.getOriginalFilename(),
                    String.format("%.2f", file.getSize() / (1024.0 * 1024.0)));
            
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("Authorization 헤더가 없는 요청");
                throw new RuntimeException("Authorization 헤더가 필요합니다. Bearer 토큰을 포함해주세요.");
            }
            
            if (!authorization.startsWith("Bearer ")) {
                log.warn("잘못된 Authorization 헤더 형식: {}", authorization);
                throw new RuntimeException("Authorization 헤더는 'Bearer <token>' 형식이어야 합니다.");
            }
            
            // JWT 토큰에서 사용자 정보 추출
            User user = jwtUtil.getUserFromHeader(authorization);
            log.info("인증된 사용자: {} (ID: {})", user.getNickname(), user.getId());
            
            // 향수 생성
            PerfumeResponseDto response = perfumeService.createPerfume(sourceType, file, user);
            
            log.info("향수 생성 성공 - 향수 ID: {}, 사용자: {}", response.getId(), user.getNickname());
            
            return ApiResponse.success(response);
            
        } catch (RuntimeException e) {
            log.error("향수 생성 실패 - 사용자 오류: {}", e.getMessage());
            throw e; // RuntimeException은 그대로 던져서 GlobalExceptionHandler에서 처리
        } catch (Exception e) {
            log.error("향수 생성 실패 - 시스템 오류: ", e);
            throw new RuntimeException("향수 생성 중 예상하지 못한 오류가 발생했습니다.");
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "향수 조회",
        description = "향수 ID로 향수 정보를 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "향수 조회 성공",
            content = @Content(schema = @Schema(implementation = PerfumeResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "향수를 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 ID 형식"
        )
    })
    public ApiResponse<PerfumeResponseDto> getPerfume(
            @Parameter(description = "향수 ID", required = true)
            @PathVariable Long id) {
        
        try {
            log.info("향수 조회 요청 - id: {}", id);
            
            PerfumeResponseDto response = perfumeService.getPerfume(id);
            
            log.info("향수 조회 성공 - id: {}", id);
            
            return ApiResponse.success(response);
            
        } catch (RuntimeException e) {
            log.error("향수 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("향수 조회 실패 - 시스템 오류: ", e);
            throw new RuntimeException("향수 조회 중 예상하지 못한 오류가 발생했습니다.");
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "사용자별 향수 목록 조회",
        description = "특정 사용자가 생성한 모든 향수 목록을 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "향수 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PerfumeResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 사용자 ID 형식"
        )
    })
    public ApiResponse<List<PerfumeResponseDto>> getUserPerfumes(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {
        
        try {
            log.info("사용자 향수 목록 조회 요청 - userId: {}", userId);
            
            List<PerfumeResponseDto> response = perfumeService.getUserPerfumes(userId);
            
            log.info("사용자 향수 목록 조회 성공 - userId: {}, 향수 개수: {}", userId, response.size());
            
            return ApiResponse.success(response);
            
        } catch (RuntimeException e) {
            log.error("사용자 향수 목록 조회 실패 - userId: {}, 오류: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("사용자 향수 목록 조회 실패 - 시스템 오류: ", e);
            throw new RuntimeException("향수 목록 조회 중 예상하지 못한 오류가 발생했습니다.");
        }
    }

    @GetMapping("/my")
    @Operation(
        summary = "내 향수 목록 조회",
        description = "현재 로그인한 사용자가 생성한 모든 향수 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 향수 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PerfumeResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ApiResponse<List<PerfumeResponseDto>> getMyPerfumes(HttpServletRequest request) {
        
        try {
            // Authorization 헤더 추출 및 검증
            String authorization = request.getHeader("Authorization");
            
            if (authorization == null || authorization.trim().isEmpty()) {
                throw new RuntimeException("Authorization 헤더가 필요합니다.");
            }
            
            // JWT 토큰에서 사용자 정보 추출
            User user = jwtUtil.getUserFromHeader(authorization);
            
            log.info("내 향수 목록 조회 요청 - 사용자: {} (ID: {})", user.getNickname(), user.getId());
            
            List<PerfumeResponseDto> response = perfumeService.getUserPerfumes(user.getId());
            
            log.info("내 향수 목록 조회 성공 - 사용자: {}, 향수 개수: {}", user.getNickname(), response.size());
            
            return ApiResponse.success(response);
            
        } catch (RuntimeException e) {
            log.error("내 향수 목록 조회 실패 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("내 향수 목록 조회 실패 - 시스템 오류: ", e);
            throw new RuntimeException("내 향수 목록 조회 중 예상하지 못한 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "향수 삭제",
        description = "향수 ID로 향수를 삭제합니다. 본인이 생성한 향수만 삭제할 수 있습니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "향수 삭제 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "향수를 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "삭제 권한 없음 (본인이 생성한 향수가 아님)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ApiResponse<String> deletePerfume(
            @Parameter(description = "향수 ID", required = true)
            @PathVariable Long id,
            
            HttpServletRequest request) {
        
        try {
            log.info("향수 삭제 요청 - id: {}", id);
            
            // Authorization 헤더 추출 및 검증
            String authorization = request.getHeader("Authorization");
            
            if (authorization == null || authorization.trim().isEmpty()) {
                throw new RuntimeException("Authorization 헤더가 필요합니다.");
            }
            
            // JWT 토큰에서 사용자 정보 추출
            User user = jwtUtil.getUserFromHeader(authorization);
            log.info("향수 삭제 요청 - 향수 ID: {}, 사용자: {} (ID: {})", id, user.getNickname(), user.getId());
            
            perfumeService.deletePerfume(id, user);
            
            log.info("향수 삭제 성공 - 향수 ID: {}, 사용자: {}", id, user.getNickname());
            
            return ApiResponse.success("향수가 성공적으로 삭제되었습니다.");
            
        } catch (RuntimeException e) {
            log.error("향수 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("향수 삭제 실패 - 시스템 오류: ", e);
            throw new RuntimeException("향수 삭제 중 예상하지 못한 오류가 발생했습니다.");
        }
    }

    @GetMapping("/recommend")
    @Operation(
        summary = "향수 추천",
        description = "오디오 또는 이미지 타입에 따른 향수를 최대 10개 추천합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "향수 추천 성공",
            content = @Content(schema = @Schema(implementation = PerfumeResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (잘못된 sourceType)"
        )
    })
    public ApiResponse<List<PerfumeResponseDto>> recommendPerfume(
            @Parameter(description = "소스 타입 (AUDIO 또는 IMAGE)", required = true)
            @RequestParam("sourceType") String sourceType) {
        
        try {
            log.info("향수 추천 요청 - sourceType: {}", sourceType);
            
            // sourceType을 내부 enum으로 변환
            SourceType internalSourceType;
            if ("AUDIO".equalsIgnoreCase(sourceType)) {
                internalSourceType = SourceType.RECOMMEND_AUDIO;
            } else if ("IMAGE".equalsIgnoreCase(sourceType)) {
                internalSourceType = SourceType.RECOMMEND_IMAGE;
            } else {
                throw new RuntimeException("잘못된 sourceType입니다. AUDIO 또는 IMAGE만 사용 가능합니다.");
            }
            
            List<PerfumeResponseDto> response = perfumeService.recommendPerfumes(internalSourceType);
            
            log.info("향수 추천 성공 - sourceType: {}, 추천 개수: {}", sourceType, response.size());
            
            return ApiResponse.success(response);
            
        } catch (RuntimeException e) {
            log.error("향수 추천 실패 - sourceType: {}, 오류: {}", sourceType, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("향수 추천 실패 - 시스템 오류: ", e);
            throw new RuntimeException("향수 추천 중 예상하지 못한 오류가 발생했습니다.");
        }
    }
}