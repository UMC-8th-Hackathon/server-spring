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
                log.info("===== Whisper API 원본 응답 =====");
                log.info("추출된 가사 원문: {}", lyrics);
                log.info("가사 길이: {} 자", lyrics.length());
                log.info("===============================");
                
                // 2. 가사 검증 및 처리
                String processedLyrics = validateAndProcessLyrics(lyrics);
                log.info("처리된 가사: {}", processedLyrics);
                
                // 3. 가사를 분석하여 향수 레시피 생성
                String prompt = createLyricsAnalysisPrompt(processedLyrics);
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
     * 가사 검증 및 처리 - 강화된 무한 반복 텍스트 필터링
     */
    private String validateAndProcessLyrics(String lyrics) {
        if (lyrics == null || lyrics.trim().isEmpty()) {
            log.warn("추출된 가사가 비어있습니다.");
            return "감성적인 멜로디가 돋보이는 음악입니다.";
        }
        
        log.info("가사 검증 시작 - 원본 길이: {}", lyrics.length());
        
        // 1. 기본 정제
        String processed = lyrics.trim();
        
        // 2. 단순 반복 패턴 감지 및 제거
        processed = removeSimpleRepetition(processed);
        
        // 3. 무의미한 단어 반복 제거
        processed = removeMeaninglessRepetition(processed);
        
        // 4. 길이 제한
        if (processed.length() > 500) {
            processed = processed.substring(0, 500) + "...";
            log.info("가사가 너무 길어서 500자로 제한했습니다.");
        }
        
        // 5. 최종 유효성 검증
        if (isInvalidLyrics(processed)) {
            log.warn("의미 있는 가사를 추출하지 못했습니다. 기본 메시지로 대체합니다.");
            log.warn("무효한 가사 내용: {}", processed.substring(0, Math.min(100, processed.length())));
            return "다양한 감정이 담긴 음악으로, 리듬감과 멜로디가 조화를 이루는 곡입니다.";
        }
        
        log.info("가사 검증 완료 - 처리 후 길이: {}", processed.length());
        return processed;
    }

    /**
     * 단순 반복 패턴 제거
     */
    private String removeSimpleRepetition(String text) {
        // 같은 단어가 5번 이상 연속으로 반복되는 경우 제거
        String[] patterns = {"랩", "자, 그럼", "그러면", "해볼까요", "네!", "꿈꾸는 건 좋지만", 
                           "이 곡은 아시아의 힙합 가수의 곡인데", "이걸 이스키는 여전히"};
        
        String result = text;
        for (String pattern : patterns) {
            // 패턴이 5번 이상 반복되면 한 번만 남기기
            String regex = "(" + pattern.replace("(", "\\(").replace(")", "\\)") + "[\\s,]*){5,}";
            result = result.replaceAll(regex, pattern + " ");
        }
        
        return result;
    }

    /**
     * 무의미한 단어 반복 제거
     */
    private String removeMeaninglessRepetition(String text) {
        // 연속된 공백 제거
        text = text.replaceAll("\\s+", " ");
        
        // 같은 문장이 3번 이상 반복되는 경우 제거
        String[] sentences = text.split("[.!?]");
        StringBuilder result = new StringBuilder();
        
        String lastSentence = "";
        int repeatCount = 0;
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            if (sentence.equals(lastSentence)) {
                repeatCount++;
                if (repeatCount <= 1) { // 최대 2번까지만 허용
                    result.append(sentence).append(". ");
                }
            } else {
                repeatCount = 0;
                result.append(sentence).append(". ");
                lastSentence = sentence;
            }
        }
        
        return result.toString().trim();
    }

    /**
     * 무효한 가사인지 판단 - 강화된 검증
     */
    private boolean isInvalidLyrics(String lyrics) {
        // 1. 너무 짧은 경우
        if (lyrics.length() < 20) {
            log.warn("가사가 너무 짧습니다: {} 자", lyrics.length());
            return true;
        }
        
        // 2. 단일 단어만 반복되는 경우
        String[] words = lyrics.split("\\s+");
        if (words.length > 10) {
            String firstWord = words[0];
            long sameWordCount = java.util.Arrays.stream(words)
                    .filter(word -> word.equals(firstWord))
                    .count();
            
            if (sameWordCount > words.length * 0.8) { // 80% 이상이 같은 단어
                log.warn("단일 단어 반복 감지: '{}' - {}/{}", firstWord, sameWordCount, words.length);
                return true;
            }
        }
        
        // 3. 무의미한 패턴만 있는 경우
        String cleanText = lyrics.toLowerCase()
                .replaceAll("랩", "")
                .replaceAll("자, 그럼", "")
                .replaceAll("해볼까요", "")
                .replaceAll("네!", "")
                .replaceAll("그러면", "")
                .replaceAll("[\\s\\p{Punct}]", "");
        
        if (cleanText.length() < 10) {
            log.warn("의미 있는 내용이 부족합니다. 정제 후 길이: {}", cleanText.length());
            return true;
        }
        
        return false;
    }

    /**
     * Whisper API를 사용하여 음성을 텍스트로 변환 - 강화된 설정
     */
    private String transcribeAudioToText(MultipartFile audioFile) {
        try {
            log.info("===== Whisper API 호출 시작 =====");
            log.info("파일명: {}", audioFile.getOriginalFilename());
            log.info("파일 크기: {} bytes", audioFile.getSize());
            log.info("Content Type: {}", audioFile.getContentType());
            
            // 파일 크기 검증
            if (audioFile.getSize() > 25 * 1024 * 1024) { // 25MB 제한
                throw new RuntimeException("파일 크기가 너무 큽니다. 25MB 이하의 파일을 업로드해주세요.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            // MultiValueMap을 사용하여 multipart/form-data 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 파일을 ByteArrayResource로 변환
            ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    String originalName = audioFile.getOriginalFilename();
                    // 파일명에 특수문자가 있으면 제거
                    if (originalName != null) {
                        return originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
                    }
                    return "audio.mp3";
                }
            };
            
            body.add("file", fileResource);
            body.add("model", "whisper-1");
            
            // 언어 설정 개선
            body.add("language", "ko"); // 한국어 명시적 지정
            body.add("response_format", "verbose_json"); // 더 자세한 응답 형식
            body.add("temperature", 0.0); // 가장 정확한 변환
            body.add("prompt", "음악, 가사, 노래, 힙합, 랩"); // 컨텍스트 힌트 제공

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Whisper API 요청 전송 중...");
            ResponseEntity<String> response = restTemplate.postForEntity(
                transcriptionUrl, 
                requestEntity, 
                String.class
            );

            log.info("Whisper API 응답 상태: {}", response.getStatusCode());
            log.info("Whisper API 원본 응답: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                String transcription = extractTranscriptionFromResponse(response.getBody());
                log.info("추출된 텍스트: {}", transcription);
                log.info("===== Whisper API 호출 완료 =====");
                return transcription;
            } else {
                log.warn("Whisper API 호출 실패: {}", response.getStatusCode());
                throw new RuntimeException("음성 변환에 실패했습니다");
            }

        } catch (Exception e) {
            log.error("===== Whisper API 오류 =====");
            log.error("음성 변환 중 오류: ", e);
            log.error("========================");
            throw new RuntimeException("음성을 텍스트로 변환하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Whisper API 응답에서 텍스트 추출 - 개선된 파싱
     */
    private String extractTranscriptionFromResponse(String responseBody) {
        try {
            log.info("Whisper 응답 파싱 시작");
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // verbose_json 형식 파싱
            String transcription = rootNode.path("text").asText();
            
            // 추가 정보 로깅
            if (rootNode.has("language")) {
                log.info("감지된 언어: {}", rootNode.path("language").asText());
            }
            if (rootNode.has("duration")) {
                log.info("오디오 길이: {} 초", rootNode.path("duration").asDouble());
            }
            
            if (transcription == null || transcription.trim().isEmpty()) {
                log.warn("Whisper API에서 빈 텍스트가 반환되었습니다");
                log.warn("전체 응답: {}", responseBody);
                return "음성을 인식할 수 없습니다.";
            }
            
            log.info("텍스트 추출 성공 - 길이: {} 자", transcription.length());
            return transcription.trim();
            
        } catch (Exception e) {
            log.error("변환 응답 파싱 오류: ", e);
            log.error("응답 내용: {}", responseBody);
            throw new RuntimeException("음성 변환 결과를 파싱하는데 실패했습니다");
        }
    }

    /**
     * 가사 분석을 위한 개선된 프롬프트 생성
     */
    private String createLyricsAnalysisPrompt(String lyrics) {
        return String.format("""
            당신은 전문 음악 분석가이자 향수 제작 전문가입니다. 
            다음 가사/음성을 분석하여 음악이 담고 있는 감정, 분위기를 파악하고,
            이를 바탕으로 향수 제작에 필요한 상세한 분석을 JSON 형태로 제공해주세요.
            
            === 분석할 텍스트 ===
            %s
            ==================
            
            위 텍스트를 분석하여 다음 요소들을 파악해주세요:
            1. 전체적인 감정과 무드 (예: 에너지틱, 차분함, 그리움, 희망 등)
            2. 음악적 분위기 (예: 힙합, 발라드, 댄스 등의 장르적 특성)
            3. 연상되는 이미지와 색감
            4. 계절감이나 시간대
            
            다음 JSON 형태로 정확히 응답해주세요:
            {
                "type": "AUDIO",
                "fileDescription": "음악이 전달하는 감정과 분위기를 2-3문장으로 설명",
                "top": ["탑노트1", "탑노트2", "탑노트3"],
                "middle": ["미들노트1", "미들노트2", "미들노트3"],
                "base": ["베이스노트1", "베이스노트2", "베이스노트3"],
                "interpretation": "향수의 전체적인 특징과 노트 조화를 설명하는 2-3문장 (100자 이내)",
                "summary": "향수의 핵심 특징을 담은 짧은 문장",
                "title": "향수 이름 : 부제목 형태 (25자 이내)"
            }
            
            제약사항:
            - JSON 형식을 정확히 지켜주세요
            - 각 배열은 정확히 3개 항목만 포함
            - 노트명: 각 4글자 이내 (예: 레몬, 장미, 바닐라)
            - 실제 존재하는 향료명만 사용
            
            감정-향료 매핑 가이드:
            - 에너지틱/힙합: 스파이시(후추, 계피), 시트러스(자몽, 라임)
            - 그리움/감성: 우디(샌달우드, 시더), 플로럴(로즈, 자스민)
            - 신선함: 유칼립투스, 민트, 베르가못
            - 따뜻함: 바닐라, 앰버, 통카빈
            - 신비로움: 패출리, 인센스, 오우드
            """, lyrics);
    }

    /**
     * 이미지 분석용 프롬프트
     */
    private String createImagePrompt() {
        return """
            당신은 전문 이미지 분석가이자 향수 제작 전문가입니다. 
            이 이미지를 향수 제작 관점에서 분석하여 JSON으로 제공해주세요.
            
            다음 JSON 형태로 응답해주세요:
            {
                "type": "IMAGE",
                "fileDescription": "이미지가 전달하는 감정과 분위기를 2-3문장으로 설명",
                "top": ["탑노트1", "탑노트2", "탑노트3"],
                "middle": ["미들노트1", "미들노트2", "미들노트3"],
                "base": ["베이스노트1", "베이스노트2", "베이스노트3"],
                "interpretation": "향수의 전체적인 특징과 노트 조화를 설명하는 2-3문장",
                "summary": "향수의 핵심 특징을 담은 짧은 문장",
                "title": "향수 이름 : 부제목 형태"
            }
            
            제약사항:
            - JSON 형식 정확히 준수
            - 각 배열은 정확히 3개 항목
            - title: 25자 이내
            - 노트명: 각 4글자 이내
            - interpretation: 100자 이내
            - 실제 존재하는 향료명만 사용
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
            // 이미지 파일 크기 검증
            if (file.getSize() > 20 * 1024 * 1024) { // 20MB 제한
                throw new RuntimeException("이미지 파일 크기가 너무 큽니다. 20MB 이하의 파일을 업로드해주세요.");
            }

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
        try {
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}");
            
            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                log.warn("응답에서 JSON을 찾을 수 없습니다. 기본값을 반환합니다.");
                log.warn("GPT 응답 내용: {}", response);
                return null; // null 반환하여 기본값 사용하도록
            }
            
            return response.substring(startIndex, endIndex + 1);
        } catch (Exception e) {
            log.error("JSON 추출 중 오류: ", e);
            return null;
        }
    }

    /**
     * JSON 응답 유효성 검증
     */
    private void validateJsonResponse(String jsonResponse) {
        try {
            if (jsonResponse == null) {
                throw new RuntimeException("JSON 응답이 null입니다");
            }

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
                    "fileDescription": "리듬감과 에너지가 넘치는 음악으로, 현대적이고 역동적인 분위기를 전달합니다.",
                    "top": ["자몽", "후추", "라임"],
                    "middle": ["제라늄", "로즈메리", "카다몸"],
                    "base": ["시더우드", "머스크", "앰버"],
                    "interpretation": "현대 음악의 에너지틱한 비트가 담긴 향수로, 스파이시한 톱노트가 우디한 베이스와 만나 역동적인 조화를 이룹니다.",
                    "summary": "에너지와 리듬감을 담은 모던 스파이시 향수",
                    "title": "Urban Beat : 도시의 리듬"
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