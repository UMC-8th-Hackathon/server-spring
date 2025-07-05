package com.umc.domain.perfume.service;

import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
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
            
            // 2. 가상 URL 생성 (실제 파일 저장 없음)
            String virtualFileUrl = generateVirtualFileUrl(file);
            
            // 3. GPT를 통한 향수 정보 생성 (파일 직접 전달)
            Perfume perfume = perfumeGptService.generatePerfume(sourceType, virtualFileUrl, file);
            
            // 4. 검증된 사용자 정보 설정
            perfume.setUser(existingUser);
            
            // 5. 데이터베이스에 저장
            Perfume savedPerfume = perfumeRepository.save(perfume);
            
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
     * 가상 파일 URL 생성 (실제 파일 저장 없음)
     */
    private String generateVirtualFileUrl(MultipartFile file) {
        try {
            // 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                // 파일 형식에 따른 기본 확장자 설정
                String contentType = file.getContentType();
                if (contentType != null) {
                    if (contentType.startsWith("audio/")) {
                        extension = ".mp3";
                    } else if (contentType.startsWith("image/")) {
                        extension = ".jpg";
                    }
                } else {
                    extension = ".tmp";
                }
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            
            // 가상 URL 반환 (실제 파일 저장 없음)
            return "/virtual/" + filename;
            
        } catch (Exception e) {
            log.warn("파일 URL 생성 중 오류 발생: {}", e.getMessage());
            return "/virtual/" + UUID.randomUUID().toString() + ".tmp";
        }
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
}