package com.umc.domain.review.service;

import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.repository.PerfumeRepository;
import com.umc.domain.review.converter.ReviewConverter;
import com.umc.domain.review.dto.ReviewRequestDTO;
import com.umc.domain.review.dto.ReviewResponseDTO;
import com.umc.domain.review.entity.Review;
import com.umc.domain.review.repository.ReviewRepository;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PerfumeRepository perfumeRepository;

    @Transactional
    public ReviewResponseDTO.CreateReviewReponseDTO createReview(Long perfumeId, Long userId, ReviewRequestDTO.CreateReviewRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "리뷰 내용은 비어 있을 수 없습니다.");
        }

        Review review = ReviewConverter.toEntity(perfumeId, userId, request);
        Review saved = reviewRepository.save(review);

        return ReviewConverter.toCreateDTO(saved, user);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO.MyReviewDTO> getMyReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);

        return reviews.stream()
                .map(review -> {
                    Perfume perfume = perfumeRepository.findById(review.getPerfumeId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 향수가 존재하지 않습니다."));

                    ReviewResponseDTO.PerfumeDTO perfumeDTO = ReviewResponseDTO.PerfumeDTO.builder()
                            .id(perfume.getId())
                            .build();

                    return ReviewConverter.toMyReviewDTO(review);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO.ReviewSimpleDTO> getReviewsByPerfumeId(Long perfumeId) {
        List<Review> reviews = reviewRepository.findByPerfumeIdOrderByCreatedAtDesc(perfumeId);

        return reviews.stream().map(review -> {
            String nickname = userRepository.findById(review.getUserId())
                    .map(User::getNickname)
                    .orElse("알 수 없음");

            return ReviewConverter.toReviewSimpleDTO(review, nickname);
        }).collect(Collectors.toList());
    }
}

