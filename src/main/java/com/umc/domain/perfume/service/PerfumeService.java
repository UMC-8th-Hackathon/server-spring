package com.umc.domain.perfume.service;

import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.repository.PerfumeRepository;
import com.umc.domain.user.entity.User;
import com.umc.domain.user.repository.UserRepository;
import com.umc.global.exception.BusinessException;
import com.umc.global.exception.ErrorCode;
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
            // 0. sourceType 검증
            validateSourceType(sourceType);
            
            // 1. 사용자 존재 확인 (Foreign Key 제약 조건 해결)
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            
            // 2. 파일 유효성 검증
            validateFile(file, sourceType);
            
            // 3. GPT를 통한 향수 정보 생성 (임시 URL 사용)
            String tempUrl = "/temp/" + UUID.randomUUID().toString();
            Perfume perfume = perfumeGptService.generatePerfume(sourceType, tempUrl, file);
            
            // 4. 검증된 사용자 정보 설정
            perfume.setUser(existingUser);
            
            // 5. 데이터베이스에 저장 (ID 생성을 위해)
            Perfume savedPerfume = perfumeRepository.save(perfume);
            
            // 6. 파일을 구글 드라이브에 업로드하고 URL 업데이트
            try {
                perfumeGptService.uploadFileAndUpdateUrl(savedPerfume, file);
                log.info("파일 업로드 및 URL 업데이트 완료 - 향수 ID: {}", savedPerfume.getId());
            } catch (Exception e) {
                log.error("파일 업로드 실패 - 향수 ID: {}, 오류: {}", savedPerfume.getId(), e.getMessage());
                // 파일 업로드 실패해도 향수는 생성됨 (임시 URL 유지)
            }
            
            // 7. 업데이트된 URL로 데이터베이스 저장
            savedPerfume = perfumeRepository.save(savedPerfume);
            
            log.info("향수 생성 완료 - ID: {}, 사용자: {}, 타입: {}", 
                    savedPerfume.getId(), existingUser.getNickname(), sourceType);
            
            // 8. 응답 DTO 생성 및 반환 (sourceType을 클라이언트용으로 변환)
            PerfumeResponseDto dto = PerfumeResponseDto.from(savedPerfume);
            return dto.withClientSourceType(convertToClientSourceType(savedPerfume.getSourceType()));
            
        } catch (BusinessException e) {
            throw e; // BusinessException은 그대로 전파
        } catch (Exception e) {
            log.error("향수 생성 중 오류 발생: ", e);
            throw new BusinessException(ErrorCode.PERFUME_CREATION_FAILED);
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file, SourceType sourceType) {
        // 파일 존재 검증
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PERFUME_FILE_EMPTY);
        }

        // 파일 크기 검증
        long maxSize = sourceType == SourceType.AUDIO ? 25 * 1024 * 1024 : 20 * 1024 * 1024; // 25MB for audio, 20MB for image
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PERFUME_FILE_SIZE_EXCEEDED);
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException(ErrorCode.PERFUME_INVALID_FILE_TYPE);
        }

        if (sourceType == SourceType.AUDIO) {
            if (!contentType.startsWith("audio/")) {
                throw new BusinessException(ErrorCode.PERFUME_INVALID_FILE_TYPE);
            }
        } else if (sourceType == SourceType.IMAGE) {
            if (!contentType.startsWith("image/")) {
                throw new BusinessException(ErrorCode.PERFUME_INVALID_FILE_TYPE);
            }
        }

        log.info("파일 검증 완료 - 파일명: {}, 크기: {}MB, 형식: {}", 
                file.getOriginalFilename(), 
                String.format("%.2f", file.getSize() / (1024.0 * 1024.0)), 
                contentType);
    }

    /**
     * sourceType 유효성 검증
     */
    private void validateSourceType(SourceType sourceType) {
        if (sourceType == null) {
            throw new BusinessException(ErrorCode.PERFUME_INVALID_SOURCE_TYPE);
        }
        
        if (sourceType != SourceType.AUDIO && sourceType != SourceType.IMAGE) {
            throw new BusinessException(ErrorCode.PERFUME_INVALID_SOURCE_TYPE);
        }
        
        log.info("sourceType 검증 완료 - 타입: {}", sourceType);
    }

    /**
     * 향수 조회
     */
    @Transactional(readOnly = true)
    public PerfumeResponseDto getPerfume(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PERFUME_INVALID_INPUT_VALUE);
        }
        
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERFUME_NOT_FOUND));
        
        log.info("향수 조회 완료 - ID: {}, 사용자: {}", id, perfume.getUser().getNickname());
        
        PerfumeResponseDto dto = PerfumeResponseDto.from(perfume);
        // sourceType을 클라이언트용으로 변환
        return dto.withClientSourceType(convertToClientSourceType(perfume.getSourceType()));
    }

    /**
     * 사용자별 향수 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PerfumeResponseDto> getUserPerfumes(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        List<Perfume> perfumes = perfumeRepository.findByUserOrderByCreatedAtDesc(user);
        
        log.info("사용자 향수 목록 조회 완료 - 사용자: {}, 향수 개수: {}", user.getNickname(), perfumes.size());
        
        return perfumes.stream()
                .map(perfume -> {
                    PerfumeResponseDto dto = PerfumeResponseDto.from(perfume);
                    // sourceType을 클라이언트용으로 변환
                    return dto.withClientSourceType(convertToClientSourceType(perfume.getSourceType()));
                })
                .collect(Collectors.toList());
    }

    /**
     * 향수 삭제 (본인이 생성한 향수만 삭제 가능)
     */
    public void deletePerfume(Long id, User user) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PERFUME_INVALID_INPUT_VALUE);
        }
        
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERFUME_NOT_FOUND));
        
        // 본인이 생성한 향수인지 확인
        if (perfume.getUser() == null || !perfume.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PERFUME_ACCESS_DENIED);
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
            throw new BusinessException(ErrorCode.PERFUME_INVALID_SOURCE_TYPE);
        }
        
        if (sourceType != SourceType.RECOMMEND_AUDIO && sourceType != SourceType.RECOMMEND_IMAGE) {
            throw new BusinessException(ErrorCode.PERFUME_INVALID_SOURCE_TYPE);
        }
        
        // DB에서 추천 타입에 해당하는 향수들을 최대 10개 조회
        List<Perfume> recommendationPerfumes = perfumeRepository.findTop10BySourceTypeOrderByCreatedAtDesc(sourceType);
        
        // 향수들을 DTO로 변환하되, sourceType을 클라이언트용으로 변환
        List<PerfumeResponseDto> recommendations = recommendationPerfumes.stream()
                .map(perfume -> {
                    PerfumeResponseDto dto = PerfumeResponseDto.from(perfume);
                    // DB의 RECOMMEND_AUDIO -> 클라이언트의 AUDIO로 변환
                    // DB의 RECOMMEND_IMAGE -> 클라이언트의 IMAGE로 변환
                    return dto.withClientSourceType(convertToClientSourceType(perfume.getSourceType()));
                })
                .toList();
        
        log.info("향수 추천 완료 - DB 타입: {}, 클라이언트 타입: {}, 추천 개수: {}", 
                sourceType, convertToClientSourceType(sourceType), recommendations.size());
        
        return recommendations;
    }
    
    /**
     * DB용 SourceType을 클라이언트용으로 변환
     */
    private SourceType convertToClientSourceType(SourceType dbSourceType) {
        return switch (dbSourceType) {
            case RECOMMEND_AUDIO -> SourceType.AUDIO;
            case RECOMMEND_IMAGE -> SourceType.IMAGE;
            default -> dbSourceType;
        };
    }


}