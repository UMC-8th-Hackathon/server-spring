package com.umc.domain.file.service;

import com.umc.domain.file.dto.FileUploadResponse;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveService {

    private final Drive driveService;
    
    @Value("${google.drive.parent-folder-id}")
    private String parentFolderId;

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
        // 오디오 파일
        Map.entry("mp3", "audio/mpeg"),
        Map.entry("wav", "audio/wav"),
        Map.entry("m4a", "audio/mp4"),
        Map.entry("aac", "audio/aac"),
        Map.entry("ogg", "audio/ogg"),
        Map.entry("flac", "audio/flac"),
        // 비디오 파일
        Map.entry("mp4", "video/mp4"),
        Map.entry("avi", "video/x-msvideo"),
        Map.entry("mov", "video/quicktime"),
        Map.entry("wmv", "video/x-ms-wmv"),
        Map.entry("flv", "video/x-flv"),
        Map.entry("webm", "video/webm"),
        // 이미지 파일
        Map.entry("jpg", "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("png", "image/png"),
        Map.entry("gif", "image/gif"),
        Map.entry("webp", "image/webp"),
        Map.entry("bmp", "image/bmp"),
        Map.entry("svg", "image/svg+xml")
    );

    /**
     * 파일 업로드 메인 메서드
     */
    public FileUploadResponse uploadFile(MultipartFile multipartFile, String recordId) throws IOException {
        // 임시 파일 생성
        Path tempFile = createTempFile(multipartFile);
        
        try {
            // 파일 타입 결정
            String fileType = determineFileType(multipartFile.getContentType());
            
            // 폴더 생성 또는 찾기
            String folderId = findOrCreateFolder(recordId, fileType);
            
            // 파일 업로드
            String fileId = uploadToGoogleDrive(tempFile, multipartFile.getOriginalFilename(), 
                                               multipartFile.getContentType(), folderId);
            
            // 공개 권한 설정
            makeFilePublic(fileId);
            
            // URL 생성 - iOS에서 직접 다운로드 가능한 URL 사용
            String publicUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            String directUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            
            return new FileUploadResponse(
                fileId,
                multipartFile.getOriginalFilename(),
                publicUrl,
                directUrl,
                recordId,
                fileType,
                multipartFile.getSize(),
                multipartFile.getContentType()
            );
            
        } finally {
            // 임시 파일 삭제
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 폴더 찾기 또는 생성
     */
    private String findOrCreateFolder(String recordId, String fileType) throws IOException {
        String folderName = recordId + "_" + fileType;
        
        try {
            // 기존 폴더 검색
            String query;
            if ("root".equals(parentFolderId)) {
                query = String.format("name='%s' and mimeType='application/vnd.google-apps.folder' and 'root' in parents", folderName);
            } else {
                query = String.format("name='%s' and mimeType='application/vnd.google-apps.folder' and '%s' in parents", 
                                    folderName, parentFolderId);
            }
            
            FileList result = driveService.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .execute();
            
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                log.info("기존 폴더 찾음: {} (ID: {})", folderName, files.get(0).getId());
                return files.get(0).getId();
            }
            
            // 폴더 생성
            return createFolder(folderName);
            
        } catch (Exception e) {
            log.error("폴더 찾기/생성 실패: {}", e.getMessage());
            throw new IOException("폴더 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 폴더 생성
     */
    private String createFolder(String folderName) throws IOException {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            
            // 부모 폴더 설정
            if ("root".equals(parentFolderId)) {
                fileMetadata.setParents(Collections.singletonList("root"));
            } else {
                fileMetadata.setParents(Collections.singletonList(parentFolderId));
            }

            File file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            
            log.info("폴더 생성 완료: {} (ID: {})", folderName, file.getId());
            return file.getId();
            
        } catch (Exception e) {
            log.error("폴더 생성 실패: {}, 오류: {}", folderName, e.getMessage());
            throw new IOException("폴더 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 구글 드라이브에 파일 업로드
     */
    private String uploadToGoogleDrive(Path filePath, String fileName, String mimeType, String folderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        java.io.File uploadFile = filePath.toFile();
        FileContent mediaContent = new FileContent(mimeType, uploadFile);

        File file = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        log.info("파일 업로드 완료: {} (ID: {})", fileName, file.getId());
        return file.getId();
    }

    /**
     * 파일을 공개적으로 접근 가능하게 설정
     */
    private void makeFilePublic(String fileId) throws IOException {
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");

        driveService.permissions().create(fileId, permission).execute();
        log.info("파일 공개 권한 설정 완료: {}", fileId);
    }

    /**
     * 임시 파일 생성
     */
    private Path createTempFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename != null ? 
                          originalFilename.substring(originalFilename.lastIndexOf('.')) : ".tmp";
        
        Path tempFile = Files.createTempFile("upload_", extension);
        multipartFile.transferTo(tempFile.toFile());
        
        return tempFile;
    }

    /**
     * 파일 타입 결정
     */
    private String determineFileType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("audio/")) {
            return "audio";
        } else if (mimeType != null && mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType != null && mimeType.startsWith("image/")) {
            return "image";
        }
        return "unknown";
    }

    /**
     * 레코드의 모든 파일 조회
     */
    public Map<String, List<FileUploadResponse>> getRecordFiles(String recordId) throws IOException {
        Map<String, List<FileUploadResponse>> result = new HashMap<>();
        result.put("audio", new ArrayList<>());
        result.put("video", new ArrayList<>());
        result.put("image", new ArrayList<>());

        // 오디오 폴더 파일 조회
        String audioFolderId = findFolder(recordId, "audio");
        if (audioFolderId != null) {
            result.put("audio", getFilesInFolder(audioFolderId, recordId, "audio"));
        }

        // 비디오 폴더 파일 조회
        String videoFolderId = findFolder(recordId, "video");
        if (videoFolderId != null) {
            result.put("video", getFilesInFolder(videoFolderId, recordId, "video"));
        }

        // 이미지 폴더 파일 조회
        String imageFolderId = findFolder(recordId, "image");
        if (imageFolderId != null) {
            result.put("image", getFilesInFolder(imageFolderId, recordId, "image"));
        }

        return result;
    }

    /**
     * 폴더 찾기
     */
    private String findFolder(String recordId, String fileType) throws IOException {
        String folderName = recordId + "_" + fileType;
        String query = String.format("name='%s' and mimeType='application/vnd.google-apps.folder'", folderName);

        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        return (files != null && !files.isEmpty()) ? files.get(0).getId() : null;
    }

    /**
     * 폴더 내 파일 목록 조회
     */
    private List<FileUploadResponse> getFilesInFolder(String folderId, String recordId, String fileType) throws IOException {
        String query = String.format("'%s' in parents", folderId);
        
        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, size, createdTime)")
                .execute();

        List<FileUploadResponse> files = new ArrayList<>();
        if (result.getFiles() != null) {
            for (File file : result.getFiles()) {
                String publicUrl = "https://drive.google.com/uc?export=download&id=" + file.getId();
                String directUrl = "https://drive.google.com/uc?export=download&id=" + file.getId();
                
                files.add(new FileUploadResponse(
                    file.getId(),
                    file.getName(),
                    publicUrl,
                    directUrl,
                    recordId,
                    fileType,
                    file.getSize(),
                    file.getMimeType()
                ));
            }
        }
        
        return files;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileId) throws IOException {
        driveService.files().delete(fileId).execute();
        log.info("파일 삭제 완료: {}", fileId);
    }
} 