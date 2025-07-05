package com.umc.domain.review.service;

import com.umc.domain.review.converter.ReviewConverter;
import com.umc.domain.review.dto.ReviewRequestDTO;
import com.umc.domain.review.dto.ReviewResponseDTO;
import com.umc.domain.review.entity.Review;
import com.umc.domain.review.repository.ReviewRepository;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDTO.CreateReviewReponseDTO createReview(Long perfumeId, Long userId, ReviewRequestDTO.CreatReviewRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Review review = ReviewConverter.toEntity(perfumeId, userId, request);
        Review saved = reviewRepository.save(review);

        return ReviewConverter.toCreateDTO(saved, user);
    }
}
