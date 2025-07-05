package com.umc.domain.perfume.service;

import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.repository.PerfumeRepository;
import com.umc.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PerfumeService {

    private final PerfumeRepository perfumeRepository;
    private final PerfumeGptService perfumeGptService;

    /**
     * 향수 생성
     */
    public PerfumeResponseDto createPerfume(SourceType sourceType, MultipartFile file, User user) {
        try {
            // 1. 가상 URL 생성 (실제 파일 저장 없음)
            String virtualFileUrl = generateVirtualFileUrl(file);
            
            // 2. GPT를 통한 향수 정보 생성 (파일 직접 전달)
            Perfume perfume = perfumeGptService.generatePerfume(sourceType, virtualFileUrl, file);
            
            // 3. 사용자 정보 설정
            perfume.setUser(user);
            
            // 4. 데이터베이스에 저장
            Perfume savedPerfume = perfumeRepository.save(perfume);
            
            // 5. 응답 DTO 생성 및 반환
            return PerfumeResponseDto.from(savedPerfume);
            
        } catch (Exception e) {
            log.error("향수 생성 중 오류 발생: ", e);
            throw new RuntimeException("향수 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 가상 파일 URL 생성 (실제 파일 저장 없음)
     */
    private String generateVirtualFileUrl(MultipartFile file) {
        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".tmp";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 가상 URL 반환 (실제 파일 저장 없음)
        return "/virtual/" + filename;
    }

    /**
     * 향수 조회
     */
    @Transactional(readOnly = true)
    public PerfumeResponseDto getPerfume(Long id) {
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("향수를 찾을 수 없습니다: " + id));
        
        return PerfumeResponseDto.from(perfume);
    }
}