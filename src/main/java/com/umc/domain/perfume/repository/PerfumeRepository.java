package com.umc.domain.perfume.repository;

import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
import java.util.List;
import com.umc.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {
    
    /**
     * 사용자별 향수 목록 조회 (최신순)
     */
    List<Perfume> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 사용자별 향수 목록 조회 (생성일 기준)
     */
    List<Perfume> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 소스 타입별 향수 목록 조회
     */
    List<Perfume> findBySourceTypeOrderByCreatedAtDesc(SourceType sourceType);
    
    /**
     * 사용자별 특정 소스 타입 향수 목록 조회
     */
    List<Perfume> findByUserAndSourceTypeOrderByCreatedAtDesc(User user, SourceType sourceType);
    
    /**
     * 사용자별 향수 개수 조회
     */
    long countByUser(User user);
    
    /**
     * 특정 기간 내 생성된 향수 목록 조회
     */
    @Query("SELECT p FROM Perfume p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Perfume> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 최근 생성된 향수 N개 조회
     */
    List<Perfume> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * 사용자가 존재하는지 확인 (Foreign Key 체크용)
     */
    @Query("SELECT COUNT(p) > 0 FROM Perfume p WHERE p.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    /**
     * 소스 타입별 최근 향수 10개 조회 (추천용)
     */
    List<Perfume> findTop10BySourceTypeOrderByCreatedAtDesc(SourceType sourceType);
}