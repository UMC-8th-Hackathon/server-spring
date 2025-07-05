package com.umc.domain.review.repository;

import com.umc.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByUserId(Long userId);

    List<Review> findByPerfumeIdOrderByCreatedAtDesc(Long perfumeId);

}
