package com.umc.domain.shop.service;

import com.umc.domain.shop.entity.Shop;

import java.util.List;

public interface ShopService {
    List<Shop> findNearbyShops(double centerLat, double centerLng, double radiusKm, int limit);
}
