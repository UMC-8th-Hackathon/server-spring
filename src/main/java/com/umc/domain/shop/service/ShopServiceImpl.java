package com.umc.domain.shop.service;

import com.umc.domain.shop.entity.Shop;
import com.umc.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public List<Shop> findNearbyShops(double centerLat, double centerLng, double radiusKm, int limit) {
        // 반경 → 위도/경도 범위 변환
        double delta = radiusKm / 111.0;

        double minLat = centerLat - delta;
        double maxLat = centerLat + delta;
        double minLng = centerLng - delta;
        double maxLng = centerLng + delta;

        // 범위 내 가게 조회
        List<Shop> shops = shopRepository.findByLatitudeBetweenAndLongitudeBetween(
                minLat, maxLat, minLng, maxLng
        );

        // limit 개수만 자르기
        return shops.size() > limit ? shops.subList(0, limit) : shops;
    }
}
