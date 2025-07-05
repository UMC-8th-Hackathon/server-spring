package com.umc.domain.review.controller;

import com.umc.auth.Jwt.JwtProvider;
import com.umc.common.response.ApiResponse;
import com.umc.domain.review.dto.ReviewRequestDTO;
import com.umc.domain.review.dto.ReviewResponseDTO;
import com.umc.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        String parsedToken = token.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserId(parsedToken);

        ReviewResponseDTO.CreateReviewReponseDTO result = reviewService.createReview(perfumeId, userId, request);

        return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 등록되었습니다.", result));
    }
}