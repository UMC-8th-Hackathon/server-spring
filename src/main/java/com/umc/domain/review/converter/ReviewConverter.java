package com.umc.domain.review.converter;


import com.umc.domain.review.dto.ReviewRequestDTO;
import com.umc.domain.review.dto.ReviewResponseDTO;
import com.umc.domain.review.entity.Review;
import com.umc.domain.user.entity.User;

public class ReviewConverter {

    public static Review toEntity(Long perfumeId, Long userId, ReviewRequestDTO.CreateReviewRequestDTO request) {
        return Review.builder()
                .perfumeId(perfumeId)
                .userId(userId)
                .description(request.getDescription())
                .build();
    }

    public static ReviewResponseDTO.CreateReviewReponseDTO toCreateDTO(Review review, User user) {
        return ReviewResponseDTO.CreateReviewReponseDTO.builder()
                .id(review.getId())
                .description(review.getDescription())
                .user(
                        ReviewResponseDTO.UserDTO.builder()
                                .id(user.getId())
                                .nickname(user.getNickname())
                                .build()
                )
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public static ReviewResponseDTO.MyReviewDTO toMyReviewDTO(Review review) {
        return ReviewResponseDTO.MyReviewDTO.builder()
                .id(review.getId())
                .description(review.getDescription())
                .createdAt(review.getCreatedAt())
                .perfumeId(review.getPerfumeId())
                .build();
    }

    public static ReviewResponseDTO.ReviewSimpleDTO toReviewSimpleDTO(Review review, String nickname) {
        return ReviewResponseDTO.ReviewSimpleDTO.builder()
                .id(review.getId())
                .description(review.getDescription())
                .user(new ReviewResponseDTO.UserDTO(review.getUserId(), nickname))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}