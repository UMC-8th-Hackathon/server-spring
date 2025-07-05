package com.umc.domain.file.controller;

import com.umc.domain.file.dto.FileUploadResponse;
import com.umc.domain.file.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final GoogleDriveService googleDriveService;

    /**
     * 파일 업로드
     */
    @PostMapping("/upload/{recordId}")
    public ResponseEntity<?> uploadFile(
            @PathVariable String recordId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }

            FileUploadResponse response = googleDriveService.uploadFile(file, recordId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));
            
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "파일 업로드에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 레코드의 모든 파일 조회
     */
    @GetMapping("/record/{recordId}")
    public ResponseEntity<?> getRecordFiles(@PathVariable String recordId) {
        try {
            Map<String, List<FileUploadResponse>> files = googleDriveService.getRecordFiles(recordId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", files
            ));
            
        } catch (IOException e) {
            log.error("파일 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "파일 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        try {
            googleDriveService.deleteFile(fileId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "파일이 성공적으로 삭제되었습니다."
            ));
            
        } catch (IOException e) {
            log.error("파일 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "파일 삭제에 실패했습니다: " + e.getMessage()
            ));
        }
    }
} 