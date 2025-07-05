package com.umc.domain.perfume.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@Schema(description = "향수 응답")
public class PerfumeResponseDto {

    @Schema(description = "향수 ID", example = "1")
    private Long id;

    @Schema(description = "소스 타입", example = "AUDIO")
    private SourceType sourceType;

    @Schema(description = "향수 상세 정보")
    private PerfumeDescriptionDto description;

    @Schema(description = "파일 URL", example = "https://example.com/file.mp3")
    private String url;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    @Schema(description = "생성 시간")
    @JsonProperty("createdAt")
    private String createdAt;

    @Schema(description = "수정 시간")
    @JsonProperty("updatedAt")
    private String updatedAt;

    @Getter
    @Builder
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "123")
        private Long id;

        @Schema(description = "사용자 닉네임", example = "user123")
        private String nickname;
    }

    public static PerfumeResponseDto from(Perfume perfume) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PerfumeDescriptionDto descriptionDto = objectMapper.readValue(perfume.getDescription(), PerfumeDescriptionDto.class);
            
            return PerfumeResponseDto.builder()
                    .id(perfume.getId())
                    .sourceType(perfume.getSourceType())
                    .description(descriptionDto)
                    .url(perfume.getUrl())
                    .user(UserInfo.builder()
                            .id(1L) // 임시 사용자 ID
                            .nickname("user123") // 임시 닉네임
                            .build())
                    .createdAt(perfume.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .updatedAt(perfume.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("향수 설명 JSON 파싱에 실패했습니다: " + e.getMessage());
        }
    }
}