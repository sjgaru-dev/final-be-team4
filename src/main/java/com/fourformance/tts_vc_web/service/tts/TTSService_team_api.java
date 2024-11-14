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
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
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

//    /**
//     * 개별 텍스트 변환 메서드
//     * Google TTS API를 사용하여 입력된 텍스트를 WAV 형식으로 변환하고, 파일로 저장합니다.
//     *
//     * @param text         변환할 텍스트
//     * @param languageCode 언어 코드 (예: "ko-KR", "en-US")
//     * @param gender       성별 ("male", "female", "neutral")
//     * @param speed        말하는 속도
//     * @param volume       볼륨 조정 (데시벨)
//     * @param pitch        음의 높낮이
//     * @return 저장된 WAV 파일의 경로
//     * @throws Exception 변환 또는 파일 저장 중 오류 발생 시
//     */
    public String convertSingleText(Long id) throws Exception {
        LOGGER.info("convertSingleText(Long id) 메서드 시작: id=" + id);

        // ttsDetailRepository에서 TTSDetail 객체를 Optional로 가져옴.
        Optional<TTSDetail> ttsDetailOpt = ttsDetailRepository.findById(id);
        if (ttsDetailOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid TTS Detail ID: " + id);
        }
        TTSDetail ttsDetail = ttsDetailOpt.get();

        LOGGER.info("TTSDetail 가져오기 성공: " + ttsDetail);

        // Google TTS API 호출 전에 언어 검증 수행
        checkTextLanguage(ttsDetail.getUnitScript(), ttsDetail.getVoiceStyle().getLanguageCode());

        LOGGER.info("언어 검증 완료");

        // 파일 이름과 경로 생성
        String fileName = "tts_output_" + System.currentTimeMillis() + ".wav";
        String filePath = OUTPUT_DIR + fileName;

        LOGGER.info("파일 경로 생성: " + filePath);

        // Google TTS API 호출
        ByteString audioContent = callTTSApi(ttsDetail);

        LOGGER.info("Google TTS API 호출 완료, 오디오 변환 성공");

        // 오디오 데이터를 WAV 파일로 저장
        saveAudioContent(audioContent, filePath);

        LOGGER.info("WAV 파일 저장 성공: " + filePath);

        return filePath;
    }

//    /**
//     * Google TTS API 호출 메서드
//     * Google TTS API를 사용하여 텍스트를 WAV 형식으로 변환합니다.
//     *
//     * @param text         변환할 텍스트
//     * @param languageCode 언어 코드
//     * @param gender       성별
//     * @param speed        말하는 속도
//     * @param volume       볼륨 조정 (데시벨)
//     * @param pitch        음의 높낮이
//     * @return 변환된 오디오 콘텐츠 (ByteString)
//     */
    private ByteString callTTSApi(TTSDetail ttsDetail) {
        if (ttsDetail == null) {
            throw new IllegalArgumentException("TTSDetail 객체가 null입니다.");
        }
        if (ttsDetail.getUnitScript() == null || ttsDetail.getVoiceStyle() == null) {
            throw new IllegalArgumentException("TTSDetail의 필드 값이 비어 있습니다.");
        }

        // TTSDetail 필드 값 가져오기
        String text = ttsDetail.getUnitScript();
        String languageCode = ttsDetail.getVoiceStyle().getLanguageCode();
        String gender = ttsDetail.getVoiceStyle().getGender();
        Float speed = ttsDetail.getUnitSpeed();
        Float volume = ttsDetail.getUnitVolume();
        Float pitch = ttsDetail.getUnitPitch();

        LOGGER.info(String.format("TTS API 요청 생성: Text=%s, Language=%s, Gender=%s, Speed=%.2f, Volume=%.2f, Pitch=%.2f",
                text, languageCode, gender, speed, volume, pitch));

        // 요청 페이로드 생성
        String requestPayload = String.format(
                "Text: %s, Language: %s, Gender: %s, Speed: %.2f, Volume: %.2f, Pitch: %.2f",
                text, languageCode, gender, speed, volume, pitch
        );

        // APIStatus 생성 및 저장
        APIStatus apiStatus = APIStatus.createAPIStatus(
                null, // VCDetail이 필요 없을 경우 null 전달
                ttsDetail,
                requestPayload
        );
        apiStatusRepository.save(apiStatus);

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // TTS API 입력 구성
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // 성별 파라미터 설정
            SsmlVoiceGender ssmlGender = switch (gender.toLowerCase()) {
                case "male" -> SsmlVoiceGender.MALE;
                case "female" -> SsmlVoiceGender.FEMALE;
                default -> SsmlVoiceGender.NEUTRAL;
            };

            // 음성 및 오디오 설정
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setSsmlGender(ssmlGender)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16) // WAV 형식
                    .setSpeakingRate(speed)
                    .setVolumeGainDb(volume)
                    .setPitch(pitch)
                    .build();

            // Google TTS API 호출
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            if (response.getAudioContent().isEmpty()) {

                // APIStatus 업데이트
                apiStatus.updateResponseInfo(
                        "TTS 변환 실패: 응답의 오디오 콘텐츠가 비어 있습니다.",
                        500,
                        APIUnitStatusConst.FAILURE
                );

                throw new RuntimeException("TTS 변환 실패: 응답의 오디오 콘텐츠가 비어 있습니다.");

            }

            // 응답 페이로드 생성
            String responsePayload = String.format(
                    "AudioContentSize: %d bytes, Language: %s, Gender: %s, Speed: %.2f, Volume: %.2f, Pitch: %.2f",
                    response.getAudioContent().size(),
                    languageCode,
                    gender,
                    speed,
                    volume,
                    pitch
            );

            // APIStatus 업데이트
            apiStatus.updateResponseInfo(
                    responsePayload,
                    200,
                    APIUnitStatusConst.SUCCESS
            );
            apiStatusRepository.save(apiStatus); // 변경 사항 저장

            LOGGER.info("Google TTS API 호출 및 변환 성공");

            return response.getAudioContent();

        } catch (Exception e) {
            // 예외 발생 시 APIStatus 생성 및 저장
            String errorPayload = "Error: " + e.getMessage();
            apiStatus.updateResponseInfo(
                    errorPayload,
                    500,
                    APIUnitStatusConst.FAILURE
            );
            apiStatusRepository.save(apiStatus); // 변경 사항 저장

            LOGGER.severe("TTS API 호출 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("TTS 변환 실패", e);
        }
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
        boolean isChinese = text.matches(".*[\\u4E00-\\u9FFF].*");
        boolean isJapanese = text.matches(".*[\\u3040-\\u30FF\\u31F0-\\u31FF].*");
        boolean isEnglish = text.matches(".*[A-Za-z].*");

        switch (languageCode) {
            case "ko-KR":
                if (!isKorean) {
                    throw new IllegalArgumentException("언어 코드가 'ko-KR'로 설정되었지만, 텍스트는 한국어가 아닙니다.");
                }
                break;
            case "zh-CN":
                if (!isChinese) {
                    throw new IllegalArgumentException("언어 코드가 'zh-CN'로 설정되었지만, 텍스트는 중국어가 아닙니다.");
                }
                break;
            case "ja-JP":
                if (!isJapanese) {
                    throw new IllegalArgumentException("언어 코드가 'ja-JP'로 설정되었지만, 텍스트는 일본어가 아닙니다.");
                }
                break;
            case "en-US":
            case "en-GB":
                if (!isEnglish) {
                    throw new IllegalArgumentException(String.format("언어 코드가 '%s'로 설정되었지만, 텍스트는 영어가 아닙니다.", languageCode));
                }
                break;
            default:
                throw new IllegalArgumentException("지원되지 않는 언어 코드입니다: " + languageCode);
        }
    }

    /**
     * 전체 텍스트 변환 메서드
     * 여러 텍스트 세그먼트를 한꺼번에 변환하고, 각 WAV 파일의 경로를 반환합니다.
     *
     * @param ids 변환할 텍스트 세그먼트 리스트
     * @return 변환된 WAV 파일의 URL 리스트
     * @throws Exception 변환 또는 파일 저장 중 오류 발생 시
     */
    public List<Map<String, String>> convertAllTexts(List<Long> ids) throws Exception {
        List<Map<String, String>> fileUrls = new ArrayList<>();

        // 입력된 텍스트 세그먼트를 순차적으로 변환합니다.
        for(Long id : ids){
            Optional<TTSDetail> ttsDetailOpt = ttsDetailRepository.findById(id);

            if (ttsDetailOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid TTS Detail ID: " + id);
            }

            TTSDetail ttsDetail = ttsDetailOpt.get();
            LOGGER.info("TTSDetail 가져오기 성공: " + ttsDetail);

            //개별 텍스트 변환  호출
            String filePath = convertSingleText(id);
            fileUrls.add(Map.of("fileUrl", "/api/tts/download?path=" + filePath));


        }
        return fileUrls;
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