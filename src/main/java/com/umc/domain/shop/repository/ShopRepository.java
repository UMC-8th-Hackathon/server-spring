package com.umc.domain.shop.repository;

import com.umc.domain.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    // 위도/경도 기반 검색 (간단히 계산하는 버전)
    List<Shop> findByLatitudeBetweenAndLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng
    );
}
