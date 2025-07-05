package com.umc.domain.perfume.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umc.domain.perfume.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "향수 설명 정보")
public class PerfumeDescriptionDto {

    @Schema(description = "소스 타입", example = "AUDIO")
    private SourceType type;

    @Schema(description = "파일 설명", example = "경쾌하고 밝은 멜로디의 팝송으로, 행복하고 에너지 넘치는 분위기를 담고 있습니다.")
    @JsonProperty("fileDescription")
    private String fileDescription;

    @Schema(description = "탑노트", example = "[\"베르가못\", \"레몬\", \"오렌지\"]")
    private List<String> top;

    @Schema(description = "미들노트", example = "[\"로즈\", \"자스민\", \"라벤더\"]")
    private List<String> middle;

    @Schema(description = "베이스노트", example = "[\"머스크\", \"샌달우드\", \"바닐라\"]")
    private List<String> base;

    @Schema(description = "해석", example = "밝고 경쾌한 멜로디가 주는 따뜻한 감성을 표현한 향수로, 상쾌한 시트러스 노트가 로맨틱한 플로럴과 조화를 이루며 부드러운 우디 베이스로 마무리됩니다.")
    private String interpretation;

    @Schema(description = "요약", example = "행복과 설렘을 담은 시트러스 플로럴 향수")
    private String summary;

    @Schema(description = "제목", example = "Sunny Melody : 행복의 멜로디")
    private String title;
} 