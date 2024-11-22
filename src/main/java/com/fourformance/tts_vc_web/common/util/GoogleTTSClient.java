package com.fourformance.tts_vc_web.common.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class GoogleTTSClient {

    @Value("${google.cloud.credentials.path}")
    private String credentialsPath;

    public TextToSpeechClient createTextToSpeechClient() {
        try {
            // JSON 파일에서 인증 정보 로드
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            return TextToSpeechClient.create(settings);
        } catch (IOException e) {
            throw new RuntimeException("Google TTS 클라이언트 생성 실패: " + e.getMessage(), e);
        }
    }
}
