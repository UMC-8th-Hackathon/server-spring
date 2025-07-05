package com.umc.domain.perfume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.domain.perfume.entity.SourceType;
import com.umc.domain.perfume.entity.Perfume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerfumeGptService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.transcription.url:https://api.openai.com/v1/audio/transcriptions}")
    private String transcriptionUrl;

    /**
     * 향수 정보 전체 생성 프로세스
     */
    public Perfume generatePerfume(SourceType sourceType, String url, MultipartFile file) {
        String description = generatePerfumeDescription(sourceType, file);
        return createPerfumeEntity(sourceType, url, description);
    }

    /**
     * 파일을 분석하여 향수 정보를 생성합니다.
     */
    public String generatePerfumeDescription(SourceType sourceType, MultipartFile file) {
        try {
            if (sourceType == SourceType.AUDIO) {
                // 1. 음성을 텍스트로 변환 (가사 추출)
                String lyrics = transcribeAudioToText(file);
                log.info("추출된 가사: {}", lyrics);
                
                // 2. 가사를 분석하여 향수 레시피 생성
                String prompt = createLyricsAnalysisPrompt(lyrics);
                String gptResponse = callGptApiWithText(prompt);
                
                // GPT 응답에서 JSON 부분만 추출
                String jsonResponse = extractJsonFromResponse(gptResponse);
                
                // JSON 유효성 검증
                validateJsonResponse(jsonResponse);
                
                return jsonResponse;
            } else {
                // 이미지 파일 처리 (기존 방식)
                String prompt = createImagePrompt();
                String gptResponse = callGptApiWithImage(prompt, file);
                
                String jsonResponse = extractJsonFromResponse(gptResponse);
                validateJsonResponse(jsonResponse);
                
                return jsonResponse;
            }
            
        } catch (Exception e) {
            log.error("GPT API 호출 중 오류 발생: ", e);
            return getDefaultDescription(sourceType);
        }
    }

    /**
     * Whisper API를 사용하여 음성을 텍스트로 변환합니다.
     */
    private String transcribeAudioToText(MultipartFile audioFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            // MultiValueMap을 사용하여 multipart/form-data 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 파일을 ByteArrayResource로 변환
            ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            };
            
            body.add("file", fileResource);
            body.add("model", "whisper-1");
            body.add("language", "ko"); // 한국어 우선, 자동 감지도 가능
            body.add("response_format", "json");
            body.add("temperature", 0.2); // 낮은 temperature로 정확성 향상

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                transcriptionUrl, 
                requestEntity, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return extractTranscriptionFromResponse(response.getBody());
            } else {
                log.warn("Whisper API 호출 실패: {}", response.getStatusCode());
                throw new RuntimeException("음성 변환에 실패했습니다");
            }

        } catch (Exception e) {
            log.error("음성 변환 중 오류: ", e);
            throw new RuntimeException("음성을 텍스트로 변환하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Whisper API 응답에서 텍스트 추출
     */
    private String extractTranscriptionFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String transcription = rootNode.path("text").asText();
            
            if (transcription == null || transcription.trim().isEmpty()) {
                throw new RuntimeException("변환된 텍스트가 비어있습니다");
            }
            
            return transcription.trim();
            
        } catch (Exception e) {
            log.error("변환 응답 파싱 오류: ", e);
            throw new RuntimeException("음성 변환 결과를 파싱하는데 실패했습니다");
        }
    }

    /**
     * 가사 분석을 위한 프롬프트 생성
     */
    private String createLyricsAnalysisPrompt(String lyrics) {
        return String.format("""
            당신은 전문 가사 분석가이자 향수 제작 전문가입니다. 
            다음 가사를 분석하여 노래가 담고 있는 감정, 분위기, 메시지를 파악하고,
            이를 바탕으로 향수 제작에 필요한 상세한 분석을 JSON 형태로 제공해주세요.
            
            === 분석할 가사 ===
            %s
            ==================
            
            위 가사를 분석하여 다음 요소들을 파악해주세요:
            1. 전체적인 감정 (기쁨, 슬픔, 그리움, 사랑, 희망, 우울 등)
            2. 분위기 (밝음, 어두움, 로맨틱, 에너제틱, 차분함 등)
            3. 주요 테마와 이미지
            4. 계절감이나 시간대
            5. 색채감
            
            이 분석을 바탕으로 다음 JSON 형태로 응답해주세요:
            {
                "type": "AUDIO",
                "fileDescription": "가사가 전달하는 감정과 분위기를 2-3문장으로 설명",
                "top": ["탑노트 최대 3개"],
                "middle": ["미들노트 최대 3개"],
                "base": ["베이스노트 최대 3개"],
                "interpretation": "향수의 전체적인 특징과 노트 조화를 설명하는 2-3문장",
                "summary": "향수의 핵심 특징을 담은 짧은 문장",
                "title": "향수 이름 : 부제목 형태"
            }
            
            주의사항:
            - JSON 형식을 정확히 지켜주세요
            - 배열은 정확히 3개씩만 포함해주세요
            - title: 25자 이내로 작성해주세요
            - top/middle/base 노트: 각 항목당 4글자 이내 (예: 레몬, 장미, 바닐라 등)
            - interpretation: 100자 이내로 작성해주세요
            - 향수 노트는 실제 존재하는 향료명을 사용해주세요
            - 가사의 감정과 이미지를 향수 노트로 창의적이고 적절하게 매핑해주세요
            
            감정-향료 매핑 가이드:
            - 기쁨/밝음: 시트러스(레몬, 오렌지), 플로럴(로즈, 자스민)
            - 사랑/로맨스: 플로럴(장미, 피오니), 바닐라, 머스크
            - 그리움/슬픔: 우디(샌달우드, 시더), 스파이시(카다몸, 클로브)
            - 신선함/청량감: 아쿠아틱, 그린(eucalyptus, 민트)
            - 따뜻함/포근함: 바닐라, 앰버, 통카빈
            - 신비로움: 오리엔탈(인센스, 패출리), 우드
            """, lyrics);
    }

    /**
     * 이미지 분석용 프롬프트
     */
    private String createImagePrompt() {
        return """
            당신은 전문 이미지 분석가이자 향수 제작 전문가입니다. 이 이미지를 향수 제작에 필요한 관점에서 상세히 분석하여 JSON 형태로 제공해주세요.
            
            다음 JSON 형태로 응답해주세요:
            {
                "type": "IMAGE",
                "fileDescription": "이미지가 전달하는 감정과 분위기를 2-3문장으로 설명",
                "top": ["탑노트 최대 3개"],
                "middle": ["미들노트 최대 3개"],
                "base": ["베이스노트 최대 3개"],
                "interpretation": "향수의 전체적인 특징과 노트 조화를 설명하는 2-3문장",
                "summary": "향수의 핵심 특징을 담은 짧은 문장",
                "title": "향수 이름 : 부제목 형태"
            }
            
            주의사항:
            - JSON 형식을 정확히 지켜주세요
            - 배열은 정확히 3개씩만 포함해주세요
            - title: 25자 이내로 작성해주세요
            - top/middle/base 노트: 각 항목당 4글자 이내
            - interpretation: 100자 이내로 작성해주세요
            - 향수 노트는 실제 존재하는 향료명을 사용해주세요
            """;
    }

    /**
     * GPT API 호출 (텍스트만)
     */
    private String callGptApiWithText(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", 600);
            requestBody.put("temperature", 0.7);
            requestBody.put("messages", List.of(
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return extractContentFromApiResponse(response.getBody());
            } else {
                log.warn("GPT-4o-mini 분석 실패: {}", response.getStatusCode());
                throw new RuntimeException("GPT 분석에 실패했습니다");
            }
            
        } catch (Exception e) {
            log.error("GPT API 호출 중 오류: ", e);
            throw new RuntimeException("GPT API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * GPT API 호출 (이미지 포함)
     */
    private String callGptApiWithImage(String prompt, MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String base64File = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", 600);
            requestBody.put("temperature", 0.7);
            requestBody.put("messages", List.of(
                Map.of(
                    "role", "user",
                    "content", List.of(
                        Map.of(
                            "type", "text",
                            "text", prompt
                        ),
                        Map.of(
                            "type", "image_url",
                            "image_url", Map.of(
                                "url", "data:" + file.getContentType() + ";base64," + base64File
                            )
                        )
                    )
                )
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return extractContentFromApiResponse(response.getBody());
            } else {
                log.warn("GPT-4o-mini 이미지 분석 실패: {}", response.getStatusCode());
                throw new RuntimeException("이미지 분석에 실패했습니다");
            }
            
        } catch (Exception e) {
            log.error("GPT API 호출 중 오류: ", e);
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * GPT API 응답에서 content 추출
     */
    private String extractContentFromApiResponse(String apiResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode choicesNode = rootNode.path("choices");
            
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).path("message");
                return messageNode.path("content").asText();
            }
            
            throw new RuntimeException("GPT API 응답 형식이 올바르지 않습니다");
            
        } catch (Exception e) {
            log.error("GPT API 응답 파싱 오류: ", e);
            throw new RuntimeException("GPT API 응답 파싱에 실패했습니다");
        }
    }

    /**
     * GPT 응답에서 JSON 부분만 추출
     */
    private String extractJsonFromResponse(String response) {
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}");
        
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw new RuntimeException("응답에서 유효한 JSON을 찾을 수 없습니다");
        }
        
        return response.substring(startIndex, endIndex + 1);
    }

    /**
     * JSON 응답 유효성 검증
     */
    private void validateJsonResponse(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            
            String[] requiredFields = {"type", "fileDescription", "top", "middle", "base", 
                                      "interpretation", "summary", "title"};
            
            for (String field : requiredFields) {
                if (!jsonNode.has(field) || jsonNode.get(field).isNull()) {
                    throw new RuntimeException("필수 필드가 누락되었습니다: " + field);
                }
            }
            
            String[] arrayFields = {"top", "middle", "base"};
            for (String field : arrayFields) {
                JsonNode arrayNode = jsonNode.get(field);
                if (!arrayNode.isArray() || arrayNode.size() == 0) {
                    throw new RuntimeException("배열 필드가 유효하지 않습니다: " + field);
                }
            }
            
        } catch (Exception e) {
            log.error("JSON 유효성 검증 실패: ", e);
            throw new RuntimeException("생성된 JSON이 유효하지 않습니다: " + e.getMessage());
        }
    }

    /**
     * Perfume 엔티티 생성
     */
    public Perfume createPerfumeEntity(SourceType sourceType, String url, String description) {
        Perfume perfume = new Perfume();
        perfume.setSourceType(sourceType);
        perfume.setUrl(url);
        perfume.setDescription(description);
        return perfume;
    }

    /**
     * 기본 설명 반환
     */
    private String getDefaultDescription(SourceType sourceType) {
        if (sourceType == SourceType.AUDIO) {
            return """
                {
                    "type": "AUDIO",
                    "fileDescription": "따뜻하고 감성적인 노래로, 사랑과 그리움을 담은 아름다운 멜로디입니다.",
                    "top": ["베르가못", "핑크페퍼", "레몬"],
                    "middle": ["로즈", "자스민", "릴리"],
                    "base": ["샌달우드", "머스크", "바닐라"],
                    "interpretation": "노래의 감성적이고 로맨틱한 가사가 담긴 향수로, 부드러운 플로럴 노트가 따뜻한 우디 베이스와 어우러집니다.",
                    "summary": "사랑과 감성을 담은 로맨틱 플로럴 향수",
                    "title": "Lyrical Love : 가사 속 사랑"
                }
                """;
        } else {
            return """
                {
                    "type": "IMAGE",
                    "fileDescription": "따뜻하고 부드러운 색감의 이미지로, 평화롭고 안정적인 분위기를 연출합니다.",
                    "top": ["레몬", "라임", "오렌지"],
                    "middle": ["로즈", "자스민", "라벤더"],
                    "base": ["머스크", "우드", "바닐라"],
                    "interpretation": "따뜻하고 부드러운 색감이 주는 평화로운 감성을 표현한 향수로, 상쾌한 시트러스 노트가 우아한 플로럴과 조화를 이룹니다.",
                    "summary": "평화와 안정을 담은 시트러스 플로럴 향수",
                    "title": "Golden Hour : 평화의 시간"
                }
                """;
        }
    }
} 