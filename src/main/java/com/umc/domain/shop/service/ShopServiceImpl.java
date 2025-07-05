package com.umc.domain.shop.service;

import com.umc.domain.shop.dto.ShopResponseDto;
import com.umc.domain.shop.entity.Shop;
import com.umc.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.awt.geom.Point2D.distance;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public List<ShopResponseDto> findNearbyShops(double latitude, double longitude) {
        // 위도 1도 ≈ 111km / 경도는 위도에 따라 다름 (서울 기준 대략 1도 ≈ 88km)
        // 대략 20km 반경을 위도/경도 기준으로 환산하면:
        double latRange = 5.0; // 20km / 111km ≈ 0.18
        double lngRange = 5.0; // 20km / 88km ≈ 0.23

        int limit = 5;

        List<Shop> shops = shopRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitude - latRange, latitude + latRange,
                longitude - lngRange, longitude + lngRange
        );

        return shops.stream()
                .sorted(Comparator.comparingDouble(s ->
                        distance(latitude, longitude, s.getLatitude(), s.getLongitude())
                ))
                .limit(limit)
                .map(ShopResponseDto::from)
                .toList();
    }
}
