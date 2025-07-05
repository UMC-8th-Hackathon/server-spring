package com.umc.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ReviewResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewReponseDTO {
        private Long id;
        private String description;
        private UserDTO user;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String nickname;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyReviewDTO {
        private Long id;
        private Long perfumeId;
        private String description;
        private LocalDateTime createdAt;
    }

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PerfumeDTO {
            private Long id;
            private String name;
        }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
        public static class ReviewSimpleDTO { // 특정 향수의 리뷰 목록 조회
        private Long id;
        private String description;
        private UserDTO user;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}