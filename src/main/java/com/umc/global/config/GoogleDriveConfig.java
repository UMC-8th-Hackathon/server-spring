package com.umc.global.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleDriveConfig {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveConfig.class);

    @Value("${google.drive.service-account-key-path}")
    private Resource serviceAccountKeyPath;

    @Value("${google.drive.application-name}")
    private String applicationName;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Drive driveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        logger.info("Loading Google Drive service account key from: {}", serviceAccountKeyPath.getFilename());
        
        try {
            // JSON 파일 내용을 로그로 확인 (민감한 정보는 마스킹)
            String jsonContent = new String(serviceAccountKeyPath.getInputStream().readAllBytes());
            logger.info("Service account key file size: {} bytes", jsonContent.length());
            logger.info("Service account key starts with: {}", jsonContent.substring(0, Math.min(50, jsonContent.length())));
            
            // 스트림을 다시 생성
            GoogleCredentials credentials = ServiceAccountCredentials
                    .fromStream(serviceAccountKeyPath.getInputStream())
                    .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

            logger.info("Google Drive service account credentials loaded successfully");
            
            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to load Google Drive service account key: {}", e.getMessage(), e);
            throw e;
        }
    }
} 