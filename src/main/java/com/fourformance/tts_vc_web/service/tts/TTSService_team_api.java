package com.fourformance.tts_vc_web.service.tts;


import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Service
public class TTSService_team_api {

    @Autowired
    private TTSDetailRepository ttsDetailRepository;

    @Autowired
    private APIStatusRepository apiStatusRepository;

    private static final String OUTPUT_DIR = "output/"; // 출력 디렉토리
    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api.class.getName());

    // 생성자: 출력 디렉토리 생성
    public TTSService_team_api() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    /**
     * 단일 TTS 변환 메서드 (ID 기반)
     */
    public String convertSingleText(Long id) throws Exception {
        TTSDetail ttsDetail = getTTSDetailById(id);
        return convertTextToWav(ttsDetail.getUnitScript(), ttsDetail.getVoiceStyle().getLanguageCode(),
                ttsDetail.getVoiceStyle().getGender(), ttsDetail.getUnitSpeed(),
                ttsDetail.getUnitVolume(), ttsDetail.getUnitPitch());
    }

    /**
     * 텍스트 기반 TTS 변환 메서드
     */
    public String convertTextToWav(String text, String languageCode, String gender,
                                   double speed, double volume, double pitch) throws Exception {
        validateTextLanguage(text, languageCode);

        String fileName = "tts_output_" + System.currentTimeMillis() + ".wav";
        String filePath = OUTPUT_DIR + fileName;

        ByteString audioContent = callGoogleTTSApi(text, languageCode, gender, speed, volume, pitch);
        saveAudioContent(audioContent, filePath);

        LOGGER.info("WAV 파일 저장 완료: " + filePath);
        return filePath;
    }

    /**
     * TTS API 호출 메서드
     */
    private ByteString callGoogleTTSApi(String text, String languageCode, String gender,
                                        double speed, double volume, double pitch) {
        try (TextToSpeechClient client = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            SsmlVoiceGender ssmlGender = getGender(gender);

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setSsmlGender(ssmlGender)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(speed)
                    .setVolumeGainDb(volume)
                    .setPitch(pitch)
                    .build();

            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

            if (response.getAudioContent().isEmpty()) {
                throw new RuntimeException("TTS 변환 실패: 오디오 콘텐츠가 비어 있습니다.");
            }

            return response.getAudioContent();
        } catch (Exception e) {
            LOGGER.severe("Google TTS API 호출 오류: " + e.getMessage());
            throw new RuntimeException("TTS API 호출 실패", e);
        }
    }

    /**
     * 여러 텍스트 세그먼트를 변환하는 메서드
     *
     * @param texts 변환할 텍스트 세그먼트 리스트
     * @return 변환된 WAV 파일의 URL 리스트
     * @throws Exception 변환 또는 파일 저장 중 오류 발생 시
     */
    public List<Map<String, String>> convertAllTexts(List<Map<String, Object>> texts) throws Exception {
        List<Map<String, String>> fileUrls = new ArrayList<>();

        // 입력된 텍스트 세그먼트를 순차적으로 변환
        for (Map<String, Object> textData : texts) {
            String text = (String) textData.get("text");
            String languageCode = (String) textData.get("languageCode");
            String gender = (String) textData.get("gender");
            double speed = Double.parseDouble(textData.get("speed").toString());
            double volume = Double.parseDouble(textData.get("volume").toString());
            double pitch = Double.parseDouble(textData.get("pitch").toString());

            // 개별 텍스트 변환 호출
            String filePath = convertTextToWav(text, languageCode, gender, speed, volume, pitch);
            String fileUrl = "/tts/converted/download?path=" + filePath;

            // 결과 URL 추가
            fileUrls.add(Map.of("fileUrl", fileUrl));
        }

        return fileUrls;
    }


    /**
     * 성별 설정 메서드
     */
    private SsmlVoiceGender getGender(String gender) {
        switch (gender.toLowerCase()) {
            case "male":
                return SsmlVoiceGender.MALE;
            case "female":
                return SsmlVoiceGender.FEMALE;
            default:
                return SsmlVoiceGender.NEUTRAL;
        }
    }

    /**
     * TTSDetail 가져오기 메서드
     */
    private TTSDetail getTTSDetailById(Long id) {
        return ttsDetailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 TTS Detail ID: " + id));
    }

    /**
     * 언어 검증 메서드
     */
    private void validateTextLanguage(String text, String languageCode) {
        boolean isKorean = text.matches(".*[가-힣].*");
        boolean isEnglish = text.matches(".*[A-Za-z].*");

        if (languageCode.equals("ko-KR") && !isKorean) {
            throw new IllegalArgumentException("언어 코드가 'ko-KR'이지만 텍스트는 한국어가 아닙니다.");
        }
        if (languageCode.equals("en-US") && !isEnglish) {
            throw new IllegalArgumentException("언어 코드가 'en-US'이지만 텍스트는 영어가 아닙니다.");
        }
    }

    /**
     * 오디오 콘텐츠를 파일로 저장하는 메서드
     */
    private void saveAudioContent(ByteString audioContent, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(audioContent.toByteArray());
        }
    }

    /**
     * 파일 로드 메서드
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
