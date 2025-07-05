package com.umc.domain.shop.service;

import com.umc.domain.shop.dto.ShopResponseDto;
import com.umc.domain.shop.entity.Shop;
import com.umc.domain.shop.repository.ShopRepository;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.awt.geom.Point2D.distance;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public List<ShopResponseDto> findNearbyShops(double latitude, double longitude) {
        try {
            // 위도/경도 유효성 검증
            validateCoordinates(latitude, longitude);
            
            log.info("근처 매장 검색 시작 - lat: {}, lng: {}", latitude, longitude);
            
            // 위도 1도 ≈ 111km / 경도는 위도에 따라 다름 (서울 기준 대략 1도 ≈ 88km)
            // 대략 20km 반경을 위도/경도 기준으로 환산하면:
            double latRange = 4.0; // 20km / 111km ≈ 0.18
            double lngRange = 4.0; // 20km / 88km ≈ 0.23

            int limit = 5;

            List<Shop> shops = shopRepository.findByLatitudeBetweenAndLongitudeBetween(
                    latitude - latRange, latitude + latRange,
                    longitude - lngRange, longitude + lngRange
            );

            List<ShopResponseDto> result = shops.stream()
                    .sorted(Comparator.comparingDouble(s ->
                            distance(latitude, longitude, s.getLatitude(), s.getLongitude())
                    ))
                    .limit(limit)
                    .map(ShopResponseDto::from)
                    .toList();
            
            log.info("근처 매장 검색 완료 - 검색된 매장 수: {}", result.size());
            return result;
            
        } catch (BusinessException e) {
            throw e; // BusinessException은 그대로 전파
        } catch (Exception e) {
            log.error("매장 검색 중 오류 발생: ", e);
            throw new BusinessException(ErrorCode.SHOP_SEARCH_FAILED);
        }
    }
    
    /**
     * 위도/경도 유효성 검증
     */
    private void validateCoordinates(double latitude, double longitude) {
        // 위도는 -90 ~ 90, 경도는 -180 ~ 180 범위
        if (latitude < -90 || latitude > 90) {
            log.warn("잘못된 위도 값: {}", latitude);
            throw new BusinessException(ErrorCode.SHOP_INVALID_COORDINATES);
        }
        
        if (longitude < -180 || longitude > 180) {
            log.warn("잘못된 경도 값: {}", longitude);
            throw new BusinessException(ErrorCode.SHOP_INVALID_COORDINATES);
        }
        
        // NaN 또는 무한대 값 체크
        if (Double.isNaN(latitude) || Double.isInfinite(latitude) || 
            Double.isNaN(longitude) || Double.isInfinite(longitude)) {
            log.warn("유효하지 않은 좌표 값 - lat: {}, lng: {}", latitude, longitude);
            throw new BusinessException(ErrorCode.SHOP_INVALID_COORDINATES);
        }
    }
}
