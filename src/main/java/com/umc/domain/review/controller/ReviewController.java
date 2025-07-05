package com.umc.domain.review.controller;

import com.umc.auth.Jwt.JwtProvider;
import com.umc.common.response.ApiResponse;
import com.umc.domain.review.dto.ReviewRequestDTO;
import com.umc.domain.review.dto.ReviewResponseDTO;
import com.umc.domain.review.service.ReviewService;
import com.umc.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtProvider jwtTokenProvider;

    @PostMapping("/{perfumeId}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.CreateReviewReponseDTO>> createReview(
            @PathVariable Long perfumeId,
            @RequestHeader("Authorization") String token,
            @RequestBody ReviewRequestDTO.CreatReviewRequestDTO request
    ) {

        /*
        // 1. 향수 존재 여부 확인 -> 향수 엔티티 추가시 다시
        if (!perfumeRepository.existsById(perfumeId)) {
            return ResponseEntity.status(ErrorCode.PERFUME_NOT_FOUND.getStatus()).build();
        }*/

        // 2. JWT 유효성 검사 및 사용자 ID 추출
        String parsedToken = token.replace("Bearer ", "");
        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(parsedToken);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(ErrorCode.TOKEN_INVALID.getStatus()).build();
        }

        ReviewResponseDTO.CreateReviewReponseDTO result = reviewService.createReview(perfumeId, userId, request);

        return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 등록되었습니다.", result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO.MyReviewDTO>>> getMyReviews(
            @RequestHeader("Authorization") String token
    ) {
        String parsedToken = token.replace("Bearer ", "");
        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(parsedToken);
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(ErrorCode.TOKEN_INVALID.getStatus()).build();
        }

        List<ReviewResponseDTO.MyReviewDTO> result = reviewService.getMyReviews(userId);
        return ResponseEntity.ok(ApiResponse.success("내가 작성한 리뷰 목록 조회 성공", result));
    }

    @GetMapping("/{perfumeId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO.ReviewSimpleDTO>>> getReviewsByPerfume(
            @PathVariable Long perfumeId) {
        List<ReviewResponseDTO.ReviewSimpleDTO> result = reviewService.getReviewsByPerfumeId(perfumeId);

        //에러처리 나중에 추가 예정

        return ResponseEntity.ok(ApiResponse.success("리뷰 목록 조회 성공", result));
    }
}