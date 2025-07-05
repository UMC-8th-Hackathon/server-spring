package com.umc.domain.perfume.entity;

import com.umc.common.entity.BaseEntity;
import com.umc.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "perfume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfume extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "description", columnDefinition = "JSON")
    private String description; // JSON 형태로 저장될 설명 데이터

    @Column(name = "url", nullable = false)
    private String url; // 소스 URL (오디오/이미지 파일 경로)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // 향수를 생성한 사용자 (추천 향수는 null 가능)

    // BaseEntity에서 이미 id, createdAt, updatedAt을 상속받음
}