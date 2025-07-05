package com.umc.domain.perfume.controller;

import com.umc.common.response.ApiResponse;
import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.service.PerfumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "향수 API", description = "향수 생성 및 조회 API")
public class PerfumeController {

    private final PerfumeService perfumeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "향수 생성",
        description = "오디오 또는 이미지 파일을 업로드하여 향수 정보를 생성합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "향수 생성 성공",
            content = @Content(schema = @Schema(implementation = PerfumeResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 오류"
        )
    })
    public ApiResponse<PerfumeResponseDto> createPerfume(
            @Parameter(description = "소스 타입 (AUDIO 또는 IMAGE)")
            @RequestParam("sourceType") SourceType sourceType,
            
            @Parameter(description = "업로드할 파일 (오디오 또는 이미지)")
            @RequestParam("file") MultipartFile file) {
        
        log.info("향수 생성 요청 - sourceType: {}, fileName: {}", sourceType, file.getOriginalFilename());
        
        PerfumeResponseDto response = perfumeService.createPerfume(sourceType, file);
        
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
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "향수를 찾을 수 없음"
        )
    })
    public ApiResponse<PerfumeResponseDto> getPerfume(
            @Parameter(description = "향수 ID")
            @PathVariable Long id) {
        
        log.info("향수 조회 요청 - id: {}", id);
        
        PerfumeResponseDto response = perfumeService.getPerfume(id);
        
        return ApiResponse.success(response);
    }
} 