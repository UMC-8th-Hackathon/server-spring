package com.umc.domain.shop.dto;

import com.umc.domain.shop.entity.Shop;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopResponseDto {

    private Long id;
    private String title;
    private String contact;
    private String address;
    private String shopUrl;
    private String description;
    private Double latitude;
    private Double longitude;

    public static ShopResponseDto from(Shop shop) {
        return ShopResponseDto.builder()
                .id(shop.getId())
                .title(shop.getTitle())
                .contact(shop.getContact())
                .address(shop.getAddress())
                .shopUrl(shop.getShopUrl())
                .description(shop.getDescription())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .build();
    }
}
