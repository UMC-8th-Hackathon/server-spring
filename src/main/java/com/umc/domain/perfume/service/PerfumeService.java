package com.umc.domain.perfume.service;

import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
import java.util.List;
import java.util.ArrayList;
import com.umc.domain.perfume.repository.PerfumeRepository;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PerfumeService {

    private final PerfumeRepository perfumeRepository;
    private final PerfumeGptService perfumeGptService;
    private final UserRepository userRepository;

    /**
     * 향수 생성
     */
    public PerfumeResponseDto createPerfume(SourceType sourceType, MultipartFile file, User user) {
        try {
            // 0. 사용자 존재 확인 (Foreign Key 제약 조건 해결)
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다: " + user.getId()));
            
            // 1. 파일 유효성 검증
            validateFile(file, sourceType);
            
            // 2. GPT를 통한 향수 정보 생성 (임시 URL 사용)
            String tempUrl = "/temp/" + UUID.randomUUID().toString();
            Perfume perfume = perfumeGptService.generatePerfume(sourceType, tempUrl, file);
            
            // 3. 검증된 사용자 정보 설정
            perfume.setUser(existingUser);
            
            // 4. 데이터베이스에 저장 (ID 생성을 위해)
            Perfume savedPerfume = perfumeRepository.save(perfume);
            
            // 5. 파일을 구글 드라이브에 업로드하고 URL 업데이트
            try {
                perfumeGptService.uploadFileAndUpdateUrl(savedPerfume, file);
                log.info("파일 업로드 및 URL 업데이트 완료 - 향수 ID: {}", savedPerfume.getId());
            } catch (Exception e) {
                log.error("파일 업로드 실패 - 향수 ID: {}, 오류: {}", savedPerfume.getId(), e.getMessage());
                // 파일 업로드 실패해도 향수는 생성됨 (임시 URL 유지)
            }
            
            // 6. 업데이트된 URL로 데이터베이스 저장
            savedPerfume = perfumeRepository.save(savedPerfume);
            
            log.info("향수 생성 완료 - ID: {}, 사용자: {}, 타입: {}", 
                    savedPerfume.getId(), existingUser.getNickname(), sourceType);
            
            // 6. 응답 DTO 생성 및 반환
            return PerfumeResponseDto.from(savedPerfume);
            
        } catch (Exception e) {
            log.error("향수 생성 중 오류 발생: ", e);
            throw new RuntimeException("향수 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file, SourceType sourceType) {
        // 파일 존재 검증
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일이 비어있습니다.");
        }

        // 파일 크기 검증
        long maxSize = sourceType == SourceType.AUDIO ? 25 * 1024 * 1024 : 20 * 1024 * 1024; // 25MB for audio, 20MB for image
        if (file.getSize() > maxSize) {
            throw new RuntimeException("파일 크기가 너무 큽니다. " + (maxSize / (1024 * 1024)) + "MB 이하로 업로드해주세요.");
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("파일 형식을 확인할 수 없습니다.");
        }

        if (sourceType == SourceType.AUDIO) {
            if (!contentType.startsWith("audio/")) {
                throw new RuntimeException("오디오 파일만 업로드 가능합니다. 현재 파일 형식: " + contentType);
            }
        } else if (sourceType == SourceType.IMAGE) {
            if (!contentType.startsWith("image/")) {
                throw new RuntimeException("이미지 파일만 업로드 가능합니다. 현재 파일 형식: " + contentType);
            }
        }

        log.info("파일 검증 완료 - 파일명: {}, 크기: {}MB, 형식: {}", 
                file.getOriginalFilename(), 
                String.format("%.2f", file.getSize() / (1024.0 * 1024.0)), 
                contentType);
    }



    /**
     * 향수 조회
     */
    @Transactional(readOnly = true)
    public PerfumeResponseDto getPerfume(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("유효하지 않은 향수 ID입니다: " + id);
        }
        
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("향수를 찾을 수 없습니다: " + id));
        
        log.info("향수 조회 완료 - ID: {}, 사용자: {}", id, perfume.getUser().getNickname());
        
        return PerfumeResponseDto.from(perfume);
    }

    /**
     * 사용자별 향수 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PerfumeResponseDto> getUserPerfumes(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("유효하지 않은 사용자 ID입니다: " + userId);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다: " + userId));
        
        List<Perfume> perfumes = perfumeRepository.findByUserOrderByCreatedAtDesc(user);
        
        log.info("사용자 향수 목록 조회 완료 - 사용자: {}, 향수 개수: {}", user.getNickname(), perfumes.size());
        
        return perfumes.stream()
                .map(PerfumeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 향수 삭제 (본인이 생성한 향수만 삭제 가능)
     */
    public void deletePerfume(Long id, User user) {
        if (id == null || id <= 0) {
            throw new RuntimeException("유효하지 않은 향수 ID입니다: " + id);
        }
        
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("향수를 찾을 수 없습니다: " + id));
        
        // 본인이 생성한 향수인지 확인
        if (perfume.getUser() == null || !perfume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인이 생성한 향수만 삭제할 수 있습니다.");
        }
        
        // 향수 삭제
        perfumeRepository.delete(perfume);
        log.info("향수 삭제 완료 - 향수 ID: {}, 사용자: {}", id, user.getNickname());
    }

    /**
     * 향수 추천 (인증 없이 사용 가능) - 최대 10개 반환
     */
    @Transactional(readOnly = true)
    public List<PerfumeResponseDto> recommendPerfumes(SourceType sourceType) {
        if (sourceType == null) {
            throw new RuntimeException("sourceType이 필요합니다.");
        }
        
        if (sourceType != SourceType.RECOMMEND_AUDIO && sourceType != SourceType.RECOMMEND_IMAGE) {
            throw new RuntimeException("추천 API는 RECOMMEND_AUDIO 또는 RECOMMEND_IMAGE 타입만 사용 가능합니다.");
        }
        
        // 추천 로직: 추천 타입에 해당하는 향수들을 가져옴
        List<Perfume> recentPerfumes = perfumeRepository.findTop10BySourceTypeOrderByCreatedAtDesc(sourceType);
        
        if (recentPerfumes.isEmpty()) {
            // 추천할 향수가 없으면 기본 향수들 생성
            return createDefaultRecommendations(sourceType);
        }
        
        // 향수들을 DTO로 변환
        List<PerfumeResponseDto> recommendations = recentPerfumes.stream()
                .map(PerfumeResponseDto::from)
                .toList();
        
        log.info("향수 추천 완료 - 타입: {}, 추천 개수: {}", sourceType, recommendations.size());
        
        return recommendations;
    }

    /**
     * 기본 추천 향수들 생성 (추천할 향수가 없을 때) - 최대 10개
     */
    private List<PerfumeResponseDto> createDefaultRecommendations(SourceType sourceType) {
        List<PerfumeResponseDto> defaultRecommendations = new ArrayList<>();
        
        if (sourceType == SourceType.RECOMMEND_AUDIO) {
            // 추천 오디오 타입 기본 향수들
            String[] audioDescriptions = {
                """
                {
                    "type": "AUDIO",
                    "top": ["레몬", "라임", "베르가못"],
                    "middle": ["라벤더", "로즈마리", "민트"],
                    "base": ["머스크", "우드", "앰버"],
                    "interpretation": "신선하고 상쾌한 시트러스 향이 중간의 허브 향과 조화를 이루며, 따뜻한 베이스 노트가 안정감을 더합니다.",
                    "summary": "상쾌하고 활기찬 향수",
                    "title": "상쾌한 아침 : 활기찬 에너지",
                    "fileDescription": "오디오 파일에서 추출한 상쾌하고 활기찬 분위기를 담은 향수입니다."
                }
                """,
                """
                {
                    "type": "AUDIO",
                    "top": ["오렌지", "만다린", "그레이프프루트"],
                    "middle": ["재스민", "네롤리", "베르가못"],
                    "base": ["샌달우드", "파츌리", "머스크"],
                    "interpretation": "달콤하고 상쾌한 시트러스 향이 중간의 플로럴 향과 조화를 이루며, 깊이 있는 우디 베이스가 우아함을 더합니다.",
                    "summary": "달콤하고 상쾌한 향수",
                    "title": "달콤한 오후 : 상쾌한 기분",
                    "fileDescription": "오디오 파일에서 추출한 달콤하고 상쾌한 분위기를 담은 향수입니다."
                }
                """,
                """
                {
                    "type": "AUDIO",
                    "top": ["베르가못", "핑크 페퍼", "카다몬"],
                    "middle": ["로즈", "피오니", "일랑일랑"],
                    "base": ["앰버", "바닐라", "머스크"],
                    "interpretation": "스파이시한 향이 중간의 로맨틱한 플로럴 향과 조화를 이루며, 달콤한 베이스 노트가 매력적입니다.",
                    "summary": "스파이시하고 로맨틱한 향수",
                    "title": "스파이시한 저녁 : 매력적인 분위기",
                    "fileDescription": "오디오 파일에서 추출한 스파이시하고 로맨틱한 분위기를 담은 향수입니다."
                }
                """
            };
            
            for (int i = 0; i < audioDescriptions.length; i++) {
                Perfume defaultPerfume = Perfume.builder()
                        .sourceType(sourceType)
                        .description(audioDescriptions[i])
                        .url("/virtual/recommendation-audio-" + (i + 1) + ".json")
                        .user(null) // 추천 향수는 사용자와 연결되지 않음
                        .build();
                
                defaultRecommendations.add(PerfumeResponseDto.from(defaultPerfume));
            }
        } else {
            // 추천 이미지 타입 기본 향수들
            String[] imageDescriptions = {
                """
                {
                    "type": "IMAGE",
                    "top": ["자스민", "피오니", "로즈"],
                    "middle": ["바닐라", "일랑일랑", "오키드"],
                    "base": ["샌달우드", "파츌리", "머스크"],
                    "interpretation": "우아하고 로맨틱한 플로럴 향이 중간의 달콤한 향과 조화를 이루며, 깊이 있는 우디 베이스가 신비로움을 더합니다.",
                    "summary": "우아하고 로맨틱한 향수",
                    "title": "로맨틱한 저녁 : 우아한 분위기",
                    "fileDescription": "이미지에서 추출한 우아하고 로맨틱한 분위기를 담은 향수입니다."
                }
                """,
                """
                {
                    "type": "IMAGE",
                    "top": ["라벤더", "로즈마리", "세이지"],
                    "middle": ["재스민", "네롤리", "카모마일"],
                    "base": ["머스크", "우드", "앰버"],
                    "interpretation": "차분하고 평화로운 허브 향이 중간의 부드러운 플로럴 향과 조화를 이루며, 따뜻한 베이스 노트가 안정감을 더합니다.",
                    "summary": "차분하고 평화로운 향수",
                    "title": "평화로운 아침 : 차분한 분위기",
                    "fileDescription": "이미지에서 추출한 차분하고 평화로운 분위기를 담은 향수입니다."
                }
                """,
                """
                {
                    "type": "IMAGE",
                    "top": ["베르가못", "핑크 페퍼", "카다몬"],
                    "middle": ["로즈", "피오니", "일랑일랑"],
                    "base": ["앰버", "바닐라", "머스크"],
                    "interpretation": "스파이시하고 매력적인 향이 중간의 로맨틱한 플로럴 향과 조화를 이루며, 달콤한 베이스 노트가 매력적입니다.",
                    "summary": "스파이시하고 매력적인 향수",
                    "title": "매력적인 밤 : 스파이시한 분위기",
                    "fileDescription": "이미지에서 추출한 스파이시하고 매력적인 분위기를 담은 향수입니다."
                }
                """
            };
            
            for (int i = 0; i < imageDescriptions.length; i++) {
                Perfume defaultPerfume = Perfume.builder()
                        .sourceType(sourceType)
                        .description(imageDescriptions[i])
                        .url("/virtual/recommendation-image-" + (i + 1) + ".json")
                        .user(null) // 추천 향수는 사용자와 연결되지 않음
                        .build();
                
                defaultRecommendations.add(PerfumeResponseDto.from(defaultPerfume));
            }
        }
        
        log.info("기본 추천 향수들 생성 - 타입: {}, 개수: {}", sourceType, defaultRecommendations.size());
        
        return defaultRecommendations;
    }
}