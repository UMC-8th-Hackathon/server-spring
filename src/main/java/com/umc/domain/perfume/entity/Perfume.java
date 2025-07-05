package com.umc.domain.perfume.entity;

import com.umc.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perfume")
@Getter
@Setter
public class Perfume extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "description", columnDefinition = "JSON")
    private String description; // JSON 형태로 저장될 설명 데이터

    @Column(name = "url", nullable = false)
    private String url; // 소스 URL (오디오/이미지 파일 경로)

    // BaseEntity에서 이미 id, createdAt, updatedAt을 상속받음
} 