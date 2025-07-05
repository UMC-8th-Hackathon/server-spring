package com.umc.domain.shop.dto;

import com.umc.domain.shop.entity.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "매장 응답")
public class ShopResponseDto {

    @Schema(description = "매장 ID", example = "1")
    private Long id;

    @Schema(description = "매장명", example = "향수 전문점")
    private String title;

    @Schema(description = "연락처", example = "02-1234-5678")
    private String contact;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "매장 URL", example = "https://example.com/shop")
    private String shopUrl;

    @Schema(description = "매장 설명", example = "다양한 향수를 취급하는 전문 매장입니다.")
    private String description;

    @Schema(description = "위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
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
