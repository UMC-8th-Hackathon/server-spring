package com.umc.domain.shop.controller;

import com.umc.domain.shop.dto.ShopResponseDto;
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
            @RequestParam double lng
    ) {
        return shopService.findNearbyShops(lat, lng);
    }
}
