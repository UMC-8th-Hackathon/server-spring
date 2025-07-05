package com.umc.domain.shop.controller;

import com.umc.domain.shop.dto.ShopResponseDto;
import com.umc.domain.shop.entity.Shop;
import com.umc.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/nearby")
    public List<ShopResponseDto> getNearbyShops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "2.0") double radius, // km 단위
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<Shop> shops = shopService.findNearbyShops(lat, lng, radius, limit);
        return shops.stream()
                .map(ShopResponseDto::from)
                .toList();
    }
}
