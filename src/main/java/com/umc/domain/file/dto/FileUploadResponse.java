package com.umc.domain.file.dto;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileId;
    private String fileName;
    private String publicUrl;
    private String directUrl;
    private String recordId;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    
    public FileUploadResponse(String fileId, String fileName, String publicUrl, 
                            String directUrl, String recordId, String fileType, 
                            Long fileSize, String mimeType) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.publicUrl = publicUrl;
        this.directUrl = directUrl;
        this.recordId = recordId;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }
} 