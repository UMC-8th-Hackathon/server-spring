package com.umc.domain.perfume.service;

import com.umc.domain.perfume.dto.PerfumeResponseDto;
import com.umc.domain.perfume.entity.Perfume;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.repository.PerfumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PerfumeService {

    private final PerfumeRepository perfumeRepository;
    private final PerfumeGptService perfumeGptService;

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 향수 생성
     */
    public PerfumeResponseDto createPerfume(SourceType sourceType, MultipartFile file) {
        try {
            // 1. 파일 업로드 및 URL 생성
            String fileUrl = uploadFile(file);
            
            // 2. GPT를 통한 향수 정보 생성 (파일 직접 전달)
            Perfume perfume = perfumeGptService.generatePerfume(sourceType, fileUrl, file);
            
            // 3. 데이터베이스에 저장
            Perfume savedPerfume = perfumeRepository.save(perfume);
            
            // 4. 응답 DTO 생성 및 반환
            return PerfumeResponseDto.from(savedPerfume);
            
        } catch (Exception e) {
            log.error("향수 생성 중 오류 발생: ", e);
            throw new RuntimeException("향수 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 업로드
     */
    private String uploadFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // URL 반환 (실제 환경에서는 CDN URL로 변경)
        return "/uploads/" + filename;
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