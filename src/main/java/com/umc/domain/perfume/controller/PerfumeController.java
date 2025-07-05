package com.umc.domain.perfume.controller;

import com.umc.auth.util.JwtUtil;
import com.umc.common.response.ApiResponse;
import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.service.PerfumeService;
import java.util.List;
import com.umc.domain.user.entity.User;
import com.umc.global.config.SwaggerConfig.ApiErrorExample;
import com.umc.global.config.SwaggerConfig.ApiErrorExamples;
import com.umc.global.exception.ErrorCode;
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
        )
    })
    @ApiErrorExamples({
        ErrorCode.PERFUME_FILE_EMPTY,
        ErrorCode.PERFUME_FILE_SIZE_EXCEEDED,
        ErrorCode.PERFUME_INVALID_FILE_TYPE,
        ErrorCode.PERFUME_CREATION_FAILED,
        ErrorCode.TOKEN_INVALID,
        ErrorCode.USER_NOT_FOUND
    })
    public ApiResponse<PerfumeResponseDto> createPerfume(
            @Parameter(description = "소스 타입 (AUDIO 또는 IMAGE)", required = true)
            @RequestParam("sourceType") SourceType sourceType,
            
            @Parameter(description = "업로드할 파일 (오디오 또는 이미지)", required = true)
            @RequestParam("file") MultipartFile file,
            
            HttpServletRequest request) {
        
        log.info("향수 생성 요청 - sourceType: {}, fileName: {}, fileSize: {}MB", 
                sourceType, 
                file.getOriginalFilename(),
                String.format("%.2f", file.getSize() / (1024.0 * 1024.0)));
        
        // JWT 토큰에서 사용자 정보 추출
        User user = jwtUtil.getUserFromHeader(request.getHeader("Authorization"));
        log.info("인증된 사용자: {} (ID: {})", user.getNickname(), user.getId());
        
        // 향수 생성
        PerfumeResponseDto response = perfumeService.createPerfume(sourceType, file, user);
        
        log.info("향수 생성 성공 - 향수 ID: {}, 사용자: {}", response.getId(), user.getNickname());
        
        return ApiResponse.success(response);
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
        )
    })
    @ApiErrorExamples({
        ErrorCode.PERFUME_NOT_FOUND,
        ErrorCode.PERFUME_INVALID_INPUT_VALUE
    })
    public ApiResponse<PerfumeResponseDto> getPerfume(
            @Parameter(description = "향수 ID", required = true)
            @PathVariable Long id) {
        
        log.info("향수 조회 요청 - id: {}", id);
        
        PerfumeResponseDto response = perfumeService.getPerfume(id);
        
        log.info("향수 조회 성공 - id: {}", id);
        
        return ApiResponse.success(response);
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
        )
    })
    @ApiErrorExamples({
        ErrorCode.USER_NOT_FOUND,
        ErrorCode.INVALID_INPUT_VALUE
    })
    public ApiResponse<List<PerfumeResponseDto>> getUserPerfumes(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {
        
        log.info("사용자 향수 목록 조회 요청 - userId: {}", userId);
        
        List<PerfumeResponseDto> response = perfumeService.getUserPerfumes(userId);
        
        log.info("사용자 향수 목록 조회 성공 - userId: {}, 향수 개수: {}", userId, response.size());
        
        return ApiResponse.success(response);
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
        )
    })
    @ApiErrorExample(ErrorCode.TOKEN_INVALID)
    public ApiResponse<List<PerfumeResponseDto>> getMyPerfumes(HttpServletRequest request) {
        
        // JWT 토큰에서 사용자 정보 추출
        User user = jwtUtil.getUserFromHeader(request.getHeader("Authorization"));
        
        log.info("내 향수 목록 조회 요청 - 사용자: {} (ID: {})", user.getNickname(), user.getId());
        
        List<PerfumeResponseDto> response = perfumeService.getUserPerfumes(user.getId());
        
        log.info("내 향수 목록 조회 성공 - 사용자: {}, 향수 개수: {}", user.getNickname(), response.size());
        
        return ApiResponse.success(response);
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
        )
    })
    @ApiErrorExamples({
        ErrorCode.PERFUME_NOT_FOUND,
        ErrorCode.PERFUME_ACCESS_DENIED,
        ErrorCode.PERFUME_INVALID_INPUT_VALUE,
        ErrorCode.TOKEN_INVALID
    })
    public ApiResponse<String> deletePerfume(
            @Parameter(description = "향수 ID", required = true)
            @PathVariable Long id,
            
            HttpServletRequest request) {
        
        log.info("향수 삭제 요청 - id: {}", id);
        
        // JWT 토큰에서 사용자 정보 추출
        User user = jwtUtil.getUserFromHeader(request.getHeader("Authorization"));
        log.info("향수 삭제 요청 - 향수 ID: {}, 사용자: {} (ID: {})", id, user.getNickname(), user.getId());
        
        perfumeService.deletePerfume(id, user);
        
        log.info("향수 삭제 성공 - 향수 ID: {}, 사용자: {}", id, user.getNickname());
        
        return ApiResponse.success("향수가 성공적으로 삭제되었습니다.");
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
        )
    })
    @ApiErrorExample(ErrorCode.PERFUME_INVALID_SOURCE_TYPE)
    public ApiResponse<List<PerfumeResponseDto>> recommendPerfume(
            @Parameter(description = "소스 타입 (AUDIO 또는 IMAGE)", required = true)
            @RequestParam("sourceType") String sourceType) {

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
    }
}   