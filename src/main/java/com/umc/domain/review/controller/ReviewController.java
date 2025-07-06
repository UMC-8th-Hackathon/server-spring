package com.umc.domain.review.controller;

import com.umc.auth.Jwt.JwtProvider;
import com.umc.auth.util.JwtUtil;
import com.umc.common.response.ApiResponse;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.repository.PerfumeRepository;
import com.umc.domain.review.dto.ReviewRequestDTO;
import com.umc.domain.review.dto.ReviewResponseDTO;
import com.umc.domain.review.service.ReviewService;
import com.umc.domain.user.entity.User;
import com.umc.global.config.SwaggerConfig;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;
    private final PerfumeRepository perfumeRepository;

    @PostMapping("/{perfumeId}")
    @Operation(
            summary = "리뷰 생성",
            description = "특정 향수에 대한 리뷰를 작성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SwaggerConfig.ApiErrorExamples({
            ErrorCode.TOKEN_MISSING,
            ErrorCode.TOKEN_MALFORMED,
            ErrorCode.TOKEN_INVALID,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PERFUME_NOT_FOUND,
            ErrorCode.PERFUME_INVALID_INPUT_VALUE,
            ErrorCode.REVIEW_DESCRIPTION_EMPTY,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<ApiResponse<ReviewResponseDTO.CreateReviewReponseDTO>> createReview(
            @PathVariable Long perfumeId,
            @RequestBody ReviewRequestDTO.CreateReviewRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        log.info("리뷰 생성 요청 - perfumeId: {}", perfumeId);

        User user = jwtUtil.getUserFromHeader(httpRequest.getHeader("Authorization"));

        ReviewResponseDTO.CreateReviewReponseDTO result = reviewService.createReview(perfumeId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 등록되었습니다.", result));
    }

    @GetMapping("/me")
    @Operation(summary = "내가 작성한 리뷰 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @SwaggerConfig.ApiErrorExamples({
            ErrorCode.TOKEN_MISSING,
            ErrorCode.TOKEN_MALFORMED,
            ErrorCode.TOKEN_INVALID,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO.MyReviewDTO>>> getMyReviews(HttpServletRequest httpRequest) {
        log.info("내 리뷰 조회 요청");

        User user = jwtUtil.getUserFromHeader(httpRequest.getHeader("Authorization"));
        List<ReviewResponseDTO.MyReviewDTO> result = reviewService.getMyReviews(user.getId());
        return ResponseEntity.ok(ApiResponse.success("내가 작성한 리뷰 목록 조회 성공", result));
    }

    @GetMapping("/{perfumeId}")
    @Operation(summary = "특정 향수 리뷰 목록 조회")
    @SwaggerConfig.ApiErrorExamples({
            ErrorCode.PERFUME_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.PERFUME_INVALID_INPUT_VALUE,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO.ReviewSimpleDTO>>> getReviewsByPerfume(
            @PathVariable Long perfumeId) {

        log.info("향수 리뷰 목록 조회 요청 - perfumeId: {}", perfumeId);
        List<ReviewResponseDTO.ReviewSimpleDTO> result = reviewService.getReviewsByPerfumeId(perfumeId);
        return ResponseEntity.ok(ApiResponse.success("리뷰 목록 조회 성공", result));
    }
}