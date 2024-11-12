package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class TTSService_team_api {

    @Autowired
    TTSDetailRepository ttsDetailRepository;

    @Autowired
    APIStatusRepository apiStatusRepository;


    private static final String OUTPUT_DIR = "output/"; // WAV 파일 저장 디렉토리
    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api.class.getName());

    // 생성자: 출력 디렉토리가 존재하지 않으면 생성합니다.
    public TTSService_team_api() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    /**
     * 개별 텍스트 변환 메서드
     * Google TTS API를 사용하여 입력된 텍스트를 WAV 형식으로 변환하고, 파일로 저장합니다.
     *
     * @param text         변환할 텍스트
     * @param languageCode 언어 코드 (예: "ko-KR", "en-US")
     * @param gender       성별 ("male", "female", "neutral")
     * @param speed        말하는 속도
     * @param volume       볼륨 조정 (데시벨)
     * @param pitch        음의 높낮이
     * @return 저장된 WAV 파일의 경로
     * @throws Exception 변환 또는 파일 저장 중 오류 발생 시
     */
    public String convertSingleText(String text, String languageCode, String gender, double speed, double volume, double pitch) throws Exception {
        // Google TTS API 호출 전에 언어 검증 수행
        checkTextLanguage(text, languageCode);

        // 파일 이름과 경로 생성
        String fileName = "tts_output_" + System.currentTimeMillis() + ".wav";
        String filePath = OUTPUT_DIR + fileName;

        // Google TTS API 호출
        ByteString audioContent = callTTSApi(text, languageCode, gender, speed, volume, pitch);

        // 오디오 데이터를 WAV 파일로 저장
        saveAudioContent(audioContent, filePath);

        LOGGER.info("WAV 파일이 저장되었습니다: " + filePath);
        return filePath;
    }

    public String convertSingleText(Long id, String languageCode, String gender) throws Exception {

        Optional<TTSDetail> ttsDetail = ttsDetailRepository.findById(id);

        System.out.println("ttsDetail = " + ttsDetail);


        // Google TTS API 호출 전에 언어 검증 수행
        checkTextLanguage(ttsDetail.get().getUnitScript(), languageCode);

        // 파일 이름과 경로 생성
        String fileName = "tts_output_" + System.currentTimeMillis() + ".wav";
        String filePath = OUTPUT_DIR + fileName;

        // Google TTS API 호출
        ByteString audioContent = callTTSApi(id, ttsDetail.get().getUnitScript(), languageCode, gender, ttsDetail.get().getUnitSpeed(), ttsDetail.get().getUnitVolume(), ttsDetail.get().getUnitPitch());

        // 오디오 데이터를 WAV 파일로 저장
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

        // 입력된 텍스트 세그먼트를 순차적으로 변환합니다.
        for (Map<String, Object> textData : texts) {
            String text = (String) textData.get("text");
            String languageCode = (String) textData.get("languageCode");
            String gender = (String) textData.get("gender");
            double speed = (double) textData.get("speed");
            double volume = (double) textData.get("volume");
            double pitch = (double) textData.get("pitch");

            // 개별 텍스트 변환 호출
            String filePath = convertSingleText(text, languageCode, gender, speed, volume, pitch);
            fileUrls.add(Map.of("fileUrl", "/api/tts/download?path=" + filePath));
        }

        return fileUrls;
    }

    /**
     * 언어 검증 메서드
     * 텍스트의 언어가 선택된 언어 코드와 일치하는지 확인합니다.
     *
     * @param text         변환할 텍스트
     * @param languageCode 사용자가 선택한 언어 코드
     * @throws IllegalArgumentException 언어 불일치 시 예외 발생
     */
    private void checkTextLanguage(String text, String languageCode) {
        boolean isKorean = text.matches(".*[가-힣].*");
        boolean isEnglish = text.matches(".*[A-Za-z].*");

        if (languageCode.equals("ko-KR") && !isKorean) {
            throw new IllegalArgumentException("언어 코드가 'ko-KR'로 설정되었지만, 텍스트는 한국어가 아닙니다.");
        }
        if (languageCode.equals("en-US") && !isEnglish) {
            throw new IllegalArgumentException("언어 코드가 'en-US'로 설정되었지만, 텍스트는 영어가 아닙니다.");
        }
    }

    /**
     * Google TTS API 호출 메서드
     * Google TTS API를 사용하여 텍스트를 WAV 형식으로 변환합니다.
     *
     * @param text         변환할 텍스트
     * @param languageCode 언어 코드
     * @param gender       성별
     * @param speed        말하는 속도
     * @param volume       볼륨 조정 (데시벨)
     * @param pitch        음의 높낮이
     * @return 변환된 오디오 콘텐츠 (ByteString)
     */
    private ByteString callTTSApi(String text, String languageCode, String gender, double speed, double volume, double pitch) {
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            SsmlVoiceGender ssmlGender;
            switch (gender.toLowerCase()) {
                case "male":
                    ssmlGender = SsmlVoiceGender.MALE;
                    break;
                case "female":
                    ssmlGender = SsmlVoiceGender.FEMALE;
                    break;
                default:
                    ssmlGender = SsmlVoiceGender.NEUTRAL;
                    break;
            }

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

            SynthesizeSpeechResponse response = TextToSpeechClient.create().synthesizeSpeech(input, voice, audioConfig);
            return response.getAudioContent();
        } catch (Exception e) {
            LOGGER.severe("TTS API 호출 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("TTS 변환 실패", e);
        }
    }

    // APIStatus 생성 메서드를 반영한 메서드
    private ByteString callTTSApi(Long id, String text, String languageCode, String gender, double speed, double volume, double pitch) {
        try {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            SsmlVoiceGender ssmlGender;
            switch (gender.toLowerCase()) {
                case "male":
                    ssmlGender = SsmlVoiceGender.MALE;
                    break;
                case "female":
                    ssmlGender = SsmlVoiceGender.FEMALE;
                    break;
                default:
                    ssmlGender = SsmlVoiceGender.NEUTRAL;
                    break;
            }

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

            SynthesizeSpeechResponse response = TextToSpeechClient.create().synthesizeSpeech(input, voice, audioConfig);

            // 응답 디버깅 정보 출력
            System.out.println("Response: " + response);
            System.out.println("Audio Content Size: " + response.getAudioContent().size());

            TTSDetail ttsDetail = ttsDetailRepository.findById(id).get();

            // response AudioContent 크기가 0보다 크면 API 요청 성공 아니면 실패
            if(response.getAudioContent().size() > 0){
                APIStatus apiStatus =
                        APIStatus.createAPIStatus(null, ttsDetail, "", "", 200, APIUnitStatusConst.SUCCESS);
                apiStatusRepository.save(apiStatus);
            }else{
                APIStatus apiStatus =
                        APIStatus.createAPIStatus(null, ttsDetail, "", "", 500, APIUnitStatusConst.FAILURE);
                apiStatusRepository.save(apiStatus);

            }

            return response.getAudioContent();
        } catch (Exception e) {

            TTSDetail ttsDetail = ttsDetailRepository.findById(id).get();
            APIStatus apiStatus =
                    APIStatus.createAPIStatus(null, ttsDetail, "", "", 500, APIUnitStatusConst.FAILURE);
            apiStatusRepository.save(apiStatus);

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
     * 저장된 WAV 파일을 로드하여 다운로드 가능하도록 반환합니다.
     *
     * @param filePath 로드할 파일 경로
     * @return 로드된 파일의 리소스 객체
     * @throws IOException 파일이 존재하지 않거나 접근 불가능할 때 예외 발생
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
