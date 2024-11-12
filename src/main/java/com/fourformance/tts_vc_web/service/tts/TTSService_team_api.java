package com.fourformance.tts_vc_web.service.tts;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class TTSService_team_api {

    private static final String OUTPUT_DIR = "output/"; // WAV 파일 저장 디렉토리
    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api.class.getName());

    public TTSService_team_api() {
        // 출력 디렉토리가 존재하지 않으면 생성합니다.
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    /**
     * 개별 텍스트 변환 메서드
     * Google TTS API를 사용하여 입력된 텍스트를 WAV 형식으로 변환하고, 파일로 저장합니다.
     *
     * @param text   변환할 텍스트
     * @param speed  말하는 속도
     * @param volume 볼륨 조정 (데시벨)
     * @param pitch  음의 높낮이
     * @return 저장된 WAV 파일의 경로
     * @throws Exception 변환 또는 파일 저장 중 오류 발생 시
     */
    public String convertSingleText(String text, double speed, double volume, double pitch) throws Exception {
        String fileName = "tts_output_" + System.currentTimeMillis() + ".wav";
        String filePath = OUTPUT_DIR + fileName;

        // Google TTS API 호출
        ByteString audioContent = callTTSApi(text, speed, volume, pitch);

        // WAV 파일로 저장
        saveAudioContent(audioContent, filePath);

        LOGGER.info("WAV 파일이 저장되었습니다: " + filePath);
        return filePath;
    }

    /**
     * 전체 텍스트 변환 메서드
     * 여러 텍스트 세그먼트를 한꺼번에 변환하고, 각 WAV 파일의 경로를 반환합니다.
     *
     * @param texts 변환할 텍스트 세그먼트 리스트
     * @return 변환된 WAV 파일의 URL 리스트
     * @throws Exception 변환 또는 파일 저장 중 오류 발생 시
     */
    public List<Map<String, String>> convertAllTexts(List<Map<String, Object>> texts) throws Exception {
        List<Map<String, String>> fileUrls = new ArrayList<>();

        for (Map<String, Object> textData : texts) {
            String text = (String) textData.get("text");
            double speed = (double) textData.get("speed");
            double volume = (double) textData.get("volume");
            double pitch = (double) textData.get("pitch");

            // 개별 텍스트 변환 호출
            String filePath = convertSingleText(text, speed, volume, pitch);
            fileUrls.add(Map.of("fileUrl", "/api/tts/download?path=" + filePath));
        }

        return fileUrls;
    }

    /**
     * Google TTS API 호출 메서드
     * Google TTS API를 사용하여 텍스트를 WAV 형식으로 변환합니다.
     *
     * @param text   변환할 텍스트
     * @param speed  말하는 속도
     * @param volume 볼륨 조정 (데시벨)
     * @param pitch  음의 높낮이
     * @return 변환된 오디오 콘텐츠 (WAV 형식)
     */
    private ByteString callTTSApi(String text, double speed, double volume, double pitch) {
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16) // WAV 형식 고정
                    .setSpeakingRate(speed)
                    .setVolumeGainDb(volume)
                    .setPitch(pitch)
                    .build();

            SynthesizeSpeechResponse response = TextToSpeechClient.create().synthesizeSpeech(input, voice, audioConfig);
            return response.getAudioContent();
        } catch (Exception e) {
            LOGGER.severe("TTS API 호출 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("TTS 변환 실패", e);
        }
    }

    /**
     * 오디오 콘텐츠를 WAV 파일로 저장하는 메서드
     *
     * @param audioContent 변환된 오디오 콘텐츠
     * @param filePath     저장할 파일 경로
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    private void saveAudioContent(ByteString audioContent, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(audioContent.toByteArray());
        }
    }

    /**
     * 파일 로드 메서드
     * 저장된 파일을 로드하여 다운로드 가능하도록 반환합니다.
     *
     * @param filePath 로드할 파일 경로
     * @return 로드된 파일의 리소스 객체
     * @throws IOException 파일이 존재하지 않거나 접근 불가능할 때
     */
    public Resource loadFileAsResource(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.severe("파일을 찾을 수 없습니다: " + filePath);
            throw new IOException("파일이 존재하지 않습니다: " + filePath);
        }

        return new FileSystemResource(file);
    }
}
