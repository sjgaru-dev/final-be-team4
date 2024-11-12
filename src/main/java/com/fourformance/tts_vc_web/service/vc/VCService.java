package com.fourformance.tts_vc_web.service.vc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VCService {

    private final RestTemplate restTemplate;

    @Value("${elevenlabs.api.key}")
    private String apiKey;

    @Value("${elevenlabs.api.url}")
    private String apiUrl;

    public VCService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] convertVoice(MultipartFile originalVoice, MultipartFile targetVoice) throws IOException {

        System.out.println("API Key: " + apiKey); // API Key가 출력되는지 확인

        String voiceId = extractVoiceId(targetVoice); // 타겟 음성 파일에서 voiceId 추출

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(originalVoice.getBytes()) {
            @Override
            public String getFilename() {
                return originalVoice.getOriginalFilename();
            }
        });

        String url = apiUrl + "/speech-to-speech/" + voiceId;

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Request URL: " + url);
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            System.err.println("Authorization Header: " + headers.get("Authorization"));
            System.err.println("Request Body: " + body); // 요청 본문 로깅 추가
            throw e;
        }
    }

    private String extractVoiceId(MultipartFile targetVoice) {
        // 실제 타겟 음성 파일에서 voiceId 추출하는 로직 구현
        return "extracted_voice_id";
    }
}
