package com.umc.domain.shop.controller;

import com.umc.common.response.ApiResponse;
import com.umc.domain.shop.dto.ShopResponseDto;
import com.umc.domain.shop.service.ShopService;
import com.umc.global.config.SwaggerConfig.ApiErrorExamples;
import com.umc.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "매장 API", description = "매장 정보 조회 API")
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/nearby")
    @Operation(
        summary = "근처 매장 조회",
        description = "현재 위치(위도, 경도)를 기준으로 근처 매장 목록을 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "근처 매장 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @ApiErrorExamples({
        ErrorCode.SHOP_INVALID_COORDINATES,
        ErrorCode.SHOP_SEARCH_FAILED
    })
    public ApiResponse<List<ShopResponseDto>> getNearbyShops(
            @Parameter(description = "위도 (latitude)", required = true, example = "37.5665")
            @RequestParam double lat,
            
            @Parameter(description = "경도 (longitude)", required = true, example = "126.9780")
            @RequestParam double lng
    ) {
        log.info("근처 매장 조회 요청 - lat: {}, lng: {}", lat, lng);
        
        List<ShopResponseDto> response = shopService.findNearbyShops(lat, lng);
        
        log.info("근처 매장 조회 성공 - 매장 개수: {}", response.size());
        
        return ApiResponse.success("근처 매장이 성공적으로 조회되었습니다.", response);
    }
}
