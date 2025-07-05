package com.umc.domain.shop.service;

import com.umc.domain.shop.dto.ShopResponseDto;

import java.util.List;

public interface ShopService {
    // 반경, 리밋은 내부에서 고정 (예: 5km, 5개)
    List<ShopResponseDto> findNearbyShops(double latitude, double longitude);
}
