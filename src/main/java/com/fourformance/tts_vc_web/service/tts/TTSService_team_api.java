package com.fourformance.tts_vc_web.service.tts;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TTSService_team_api {

    private TextToSpeechClient textToSpeechClient;

    // 초기화 블록에서 Google Cloud TTS 클라이언트를 설정
    @PostConstruct
    public void init() throws IOException {
        // 리소스 폴더에서 서비스 계정 키 파일을 읽어옴
        InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream("sound-potion-440705-j8-5c7b6bb0ebd6.json");
        if (credentialsStream == null) {
            throw new IOException("Service account JSON file not found in resources.");
        }

        // GoogleCredentials 객체를 생성하여 인증 설정
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

        // TTS 클라이언트 설정을 위한 TextToSpeechSettings 생성
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        // TextToSpeechClient 생성
        textToSpeechClient = TextToSpeechClient.create(settings);
    }

    // TTS 변환 메서드
    public ByteString convertTextToSpeech(String text) throws Exception {
        // TTS 요청 구성
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();

        // 음성 설정
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build();

        // 오디오 설정 (MP3 포맷)
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        // TTS 요청 및 응답 처리
        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
        return response.getAudioContent();
    }
}
