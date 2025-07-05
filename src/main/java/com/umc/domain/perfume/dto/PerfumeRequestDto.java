package com.umc.domain.perfume.dto;

import com.umc.domain.perfume.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(description = "향수 생성 요청")
public class PerfumeRequestDto {

    @Schema(description = "소스 타입 (AUDIO 또는 IMAGE)", example = "AUDIO")
    private SourceType sourceType;

    @Schema(description = "업로드할 파일 (오디오 또는 이미지)")
    private MultipartFile file;
} 