package com.fourformance.tts_vc_web.service.tts;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class TTSService_team_api {

    private TextToSpeechClient textToSpeechClient;
    private List<String> segments = new ArrayList<>(); // 세그먼트를 저장하는 리스트


    // 초기화 블록에서 Google Cloud TTS 클라이언트를 설정
    @PostConstruct
    public void init() throws IOException {
        // 리소스 폴더에서 서비스 계정 키 파일을 읽어옴
        InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream("sound-potion-440705-j8-cc85748343a6.json");
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

    public ByteString convertTextToSpeechWithOptions(String text, double speed, double volume, double pitch) throws Exception {
        // 1. 입력 텍스트를 TTS API의 입력 형식으로 설정합니다.
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text) // 변환할 텍스트 설정
                .build();

        // 2. 음성 설정을 구성합니다.
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR") // 사용할 언어를 한국어로 설정
                .setSsmlGender(SsmlVoiceGender.NEUTRAL) // 음성의 성별을 중성으로 설정
                .build();

        // 3. 오디오 설정을 구성하여 음성의 속도, 볼륨, 피치를 사용자 지정 옵션으로 설정합니다.
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3) // 출력 오디오 형식을 MP3로 설정
                .setSpeakingRate(speed) // 말하는 속도를 사용자 입력 값으로 설정
                .setVolumeGainDb(volume) // 볼륨을 사용자 입력 값으로 설정 (단위: 데시벨)
                .setPitch(pitch) // 음의 높낮이를 사용자 입력 값으로 설정
                .build();

        // 4. TTS API를 호출하여 텍스트를 음성으로 변환하고, 응답을 받습니다.
        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

        // 5. 변환된 오디오 데이터를 반환합니다.
        return response.getAudioContent();
    }

    // 텍스트 세그먼트를 추가하는 메서드 (필요시 사용)
    public void addSegment(String text) {
        segments.add(text);
    }

    // 세그먼트 삭제 메서드
    public boolean deleteSegment(int index) {
        if (index >= 0 && index < segments.size()) {
            segments.remove(index);
            return true;
        }
        return false;
    }

    // 선택한 여러 세그먼트를 삭제하는 메서드
    public void deleteSegments(List<Integer> indexes) {
        indexes.sort((a, b) -> b - a); // 인덱스를 내림차순으로 정렬
        for (int index : indexes) {
            if (index >= 0 && index < segments.size()) {
                segments.remove(index);
            }
        }
    }
}
