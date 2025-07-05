package com.umc.domain.review.entity;

import com.umc.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    private Long perfumeId; // 연관관계 대신 ID만 저장

    private Long userId;

    private String description;
}



