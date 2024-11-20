package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Service
@Transactional
@RequiredArgsConstructor
public class TTSService_team_api3 {

    private final TTSDetailRepository ttsDetailRepository;
    private final APIStatusRepository apiStatusRepository;
    private final TTSService_team_multi ttsServiceTeamMulti; // 통합 서비스 호출
    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api3.class.getName());
    private static final String OUTPUT_DIR = "output/"; // WAV 파일 저장 디렉토리

    /**
     * 모든 TTS 디테일 처리: 신규 데이터 생성 또는 기존 데이터 업데이트
     * @param ttsSaveDto TTS 프로젝트 및 디테일 데이터를 담은 DTO
     * @return 생성된 오디오 파일 경로 리스트
     */
    public List<Map<String, String>> convertAllTtsDetails(TTSSaveDto ttsSaveDto) {
        ensureOutputDirExists();

        LOGGER.info("convertAllTtsDetails 호출: " + ttsSaveDto);

        // 프로젝트 생성 또는 업데이트
        Long projectId = Optional.ofNullable(ttsSaveDto.getProjectId())
                .map(id -> ttsServiceTeamMulti.updateProject(ttsSaveDto))
                .orElseGet(() -> ttsServiceTeamMulti.createNewProject(ttsSaveDto));

        // 프로젝트의 모든 TTS 디테일 처리
        List<Map<String, String>> fileUrls = new ArrayList<>();
        for (TTSDetailDto detail : ttsSaveDto.getTtsDetails()) {
            try {
                LOGGER.info("TTSDetail 처리 시작: " + detail);

                // 디테일 ID가 null이면 새 ID 생성
                if (detail.getId() == null) {
                    Long newDetailId = generateNewDetailId(projectId);
                    detail.setId(newDetailId);
                    LOGGER.info("새로운 디테일 ID 생성: " + newDetailId);
                }

                fileUrls.add(processTtsDetail(detail));
                LOGGER.info("TTSDetail 처리 완료: " + detail);
            } catch (Exception e) {
                LOGGER.severe("TTSDetail 처리 중 오류 발생: " + detail + ", 메시지: " + e.getMessage());
                throw new BusinessException(ErrorCode.TTS_DETAIL_PROCESSING_FAILED);
            }
        }

        LOGGER.info("convertAllTtsDetails 완료: 생성된 파일 URLs = " + fileUrls);
        return fileUrls;
    }

    /**
     * TTS 디테일 처리: Google TTS API를 통해 텍스트를 오디오로 변환하고 저장
     * @param detailDto TTS 디테일 데이터
     * @return 변환된 오디오 파일 경로
     */
    private Map<String, String> processTtsDetail(TTSDetailDto detailDto) {
        ByteString audioContent = callTTSApi(detailDto);
        String filePath = saveAudioFile(audioContent, detailDto.getUnitSequence());
        return Map.of("filePath", filePath);
    }

    /**
     * Google TTS API 호출: 텍스트를 오디오로 변환
     * @param detailDto TTS 디테일 데이터
     * @return 변환된 오디오 콘텐츠 (ByteString)
     */
    private ByteString callTTSApi(TTSDetailDto detailDto) {
//        System.out.println(detailDto.getId());
//        TTSDetail ttsDetail = ttsDetailRepository.findById(detailDto.getId()).get();
//        System.out.println("값이 안나오면:" + ttsDetail.toString());

        String languageCode = ttsDetailRepository.findVoiceStyleById(detailDto.getVoiceStyleId()).getLanguageCode();
        String gender = ttsDetailRepository.findVoiceStyleById(detailDto.getVoiceStyleId()).getGender();
        // 요청 페이로드 생성
//        String requestPayload = String.format(
//                "{ \"text\": \"%s\", \"language\": \"%s\", \"gender\": \"%s\", \"speed\": %.2f, \"volume\": %.2f, \"pitch\": %.2f }",
//                detailDto.getUnitScript(),
//                languageCode,
//                gender,
//                detailDto.getUnitSpeed(),
//                detailDto.getUnitVolume(),
//                detailDto.getUnitPitch()
//        );

//        TTSDetail ttsDetail = TTSDetail.createTTSDetail(detailDto.getProjectId(), d)

        // APIStatus 생성 및 저장
//        APIStatus apiStatus = APIStatus.createAPIStatus(null, ttsDetail, requestPayload);
//        apiStatusRepository.save(apiStatus);

        LOGGER.info("callTTSApi 호출: " + detailDto);

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(Optional.ofNullable(detailDto.getUnitScript())
                            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_UNIT_SCRIPT)))
                    .build();

            SsmlVoiceGender ssmlGender = getSsmlVoiceGender(detailDto);
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setSsmlGender(ssmlGender)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(Optional.ofNullable(detailDto.getUnitSpeed()).orElse(1.0F))
                    .setVolumeGainDb(Optional.ofNullable(detailDto.getUnitVolume()).orElse(0.0F))
                    .setPitch(Optional.ofNullable(detailDto.getUnitPitch()).orElse(0.0F))
                    .build();

            // Google TTS API 호출 전에 언어 검증 수행
//            checkTextLanguage(ttsDetail.getUnitScript(), ttsDetail.getVoiceStyle().getLanguageCode());

            // Google TTS API 호출
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

//            if (response.getAudioContent().isEmpty()) {
//                apiStatus.updateResponseInfo(
//                        "TTS 변환 실패: 응답의 오디오 콘텐츠가 비어 있습니다.",
//                        500,
//                        APIUnitStatusConst.FAILURE
//                );
//                throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED_EMPTY_CONTENT);
//            }

            // 응답 페이로드 생성
//            String responsePayload = String.format(
//                    "{ \"audioContentSize\": %d, \"language\": \"%s\", \"gender\": \"%s\", \"speed\": %.2f, \"volume\": %.2f, \"pitch\": %.2f }",
//                    response.getAudioContent().size(),
//                    ttsDetail.getVoiceStyle().getLanguageCode(),
//                    ttsDetail.getVoiceStyle().getGender(),
//                    ttsDetail.getUnitSpeed(),
//                    ttsDetail.getUnitVolume(),
//                    ttsDetail.getUnitPitch()
//            );
//
//            apiStatus.updateResponseInfo(responsePayload, 200, APIUnitStatusConst.SUCCESS);
//            apiStatusRepository.save(apiStatus);

            LOGGER.info("Google TTS API 호출 및 변환 성공");
            return response.getAudioContent();

        } catch (IOException e) {
//            String errorPayload = "Error: " + e.getMessage();
//            apiStatus.updateResponseInfo(errorPayload, 500, APIUnitStatusConst.FAILURE);
//            apiStatusRepository.save(apiStatus);

            LOGGER.severe("callTTSApi 중 예외 발생: " + e.getMessage());
            throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED);
        }
    }

    /**
     * VoiceStyle의 Gender를 SsmlVoiceGender로 변환
     * @param detailDto TTS 디테일 데이터
     * @return 변환된 SsmlVoiceGender
     */
    private SsmlVoiceGender getSsmlVoiceGender(TTSDetailDto detailDto) {
        String gender = ttsDetailRepository.findVoiceStyleById(detailDto.getVoiceStyleId()).getGender();
        return switch (gender.toLowerCase()) {
            case "male" -> SsmlVoiceGender.MALE;
            case "female" -> SsmlVoiceGender.FEMALE;
            default -> SsmlVoiceGender.NEUTRAL;
        };
    }

    /**
     * 오디오 파일 저장
     * @param audioContent Google TTS API로부터 생성된 오디오 콘텐츠
     * @param sequence 오디오 파일 순서 번호
     * @return 저장된 오디오 파일 경로
     */
    private String saveAudioFile(ByteString audioContent, int sequence) {
        String fileName = OUTPUT_DIR + "tts_audio_" + sequence + ".wav";
        LOGGER.info("saveAudioFile 호출: fileName = " + fileName);

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(audioContent.toByteArray());
            LOGGER.info("saveAudioFile 성공: " + fileName);
            return fileName;
        } catch (IOException e) {
            LOGGER.severe("saveAudioFile 실패: fileName = " + fileName + ", 메시지: " + e.getMessage());
            throw new BusinessException(ErrorCode.AUDIO_FILE_SAVE_ERROR);
        }
    }

    /**
     * 출력 디렉토리 확인 및 생성
     */
    private void ensureOutputDirExists() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new BusinessException(ErrorCode.DIRECTORY_CREATION_FAILED);
        }
    }

    /**
     * 언어 검증 메서드: 텍스트와 선택된 언어 코드의 일치 여부 확인
     * @param text 변환할 텍스트
     * @param languageCode 선택된 언어 코드
     */
    private void checkTextLanguage(String text, String languageCode) {
        boolean isKorean = text.matches(".*[가-힣].*");
        boolean isChinese = text.matches(".*[\\u4E00-\\u9FFF].*");
        boolean isJapanese = text.matches(".*[\\u3040-\\u30FF\\u31F0-\\u31FF].*");
        boolean isEnglish = text.matches(".*[A-Za-z].*");

        switch (languageCode) {
            case "ko-KR":
                if (!isKorean) throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_KO_KR);
                break;
            case "zh-CN":
                if (!isChinese) throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_ZH_CN);
                break;
            case "ja-JP":
                if (!isJapanese) throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_JA_JP);
                break;
            case "en-US":
            case "en-GB":
                if (!isEnglish) throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_EN);
                break;
            default:
                throw new BusinessException(ErrorCode.UNSUPPORTED_LANGUAGE_CODE);
        }
    }

    /**
     * 해당 프로젝트의 디테일 ID 생성 로직: 기존 ID가 없으면 1부터 시작하고 있으면 +1
     * @param projectId 프로젝트 ID
     * @return 새로 생성된 디테일 ID
     */
    private Long generateNewDetailId(Long projectId) {
        // 해당 프로젝트의 디테일 ID 목록 조회
        List<Long> detailIds = ttsDetailRepository.findDetailIdsByProjectId(projectId);

        // ID 목록 중 가장 큰 값 + 1 (없으면 1로 시작)
        return detailIds.stream()
                .max(Comparator.naturalOrder())
                .map(maxId -> maxId + 1)
                .orElse(1L); // ID가 없다면 1부터 시작
    }
}

