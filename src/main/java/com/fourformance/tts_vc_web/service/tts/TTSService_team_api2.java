package com.fourformance.tts_vc_web.service.tts;


import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailRequestDto;
import com.fourformance.tts_vc_web.dto.tts.TTSRequestDto;
import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class TTSService_team_api2 {

    @Autowired
    TTSDetailRepository ttsDetailRepository;

    @Autowired
    APIStatusRepository apiStatusRepository;

    private static final String OUTPUT_DIR = "output/"; // WAV 파일 저장 디렉토리
    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api2.class.getName());

    // 생성자: 출력 디렉토리가 존재하지 않으면 생성합니다.
    public TTSService_team_api2() {
        LOGGER.info("TTSService_team_api2 생성자 호출");
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            LOGGER.info("출력 디렉토리가 존재하지 않아 생성 중: " + OUTPUT_DIR);
            outputDir.mkdirs();
        } else {
            LOGGER.info("출력 디렉토리가 이미 존재함: " + OUTPUT_DIR);
        }
    }

    public List<Map<String, String>> convertAllTtsDetails(TTSRequestDto ttsRequestDto) {
        LOGGER.info("convertAllTtsDetails 호출: 총 " + ttsRequestDto.getTtsDetails().size() + "개의 TTS 데이터를 처리합니다.");
        
        // ProjectName 값 설정
        String projectName = ttsRequestDto.getProjectName();
        
        List<Map<String, String>> fileUrls = new ArrayList<>();

        for (TTSDetailRequestDto detail : ttsRequestDto.getTtsDetails()) {
            LOGGER.info("TTSDetail 처리 시작: " + detail);
            LOGGER.info("detail.isNew() 값 확인: " + detail.getIsNew()); // 디버깅 로그 추가
            try {
                if (detail.getIsNew()) {
                    LOGGER.info("새로운 데이터로 처리 중: " + detail);
                    Map<String, String> newFileUrl = processNewData(detail, projectName);
                    LOGGER.info("새로운 데이터 처리 완료: " + newFileUrl);
                    fileUrls.add(newFileUrl);
                } else {
                    LOGGER.info("기존 데이터로 처리 중: " + detail);
                    if (detail.getId() == null) {
                        LOGGER.severe("기존 데이터 처리 중 ID가 누락됨");
                        throw new BusinessException(ErrorCode.ID_REQUIRED_FOR_EXISTING_DATA);
                    }
                    Map<String, String> existingFileUrl = processExistingData(detail, projectName);
                    LOGGER.info("기존 데이터 처리 완료: " + existingFileUrl);
                    fileUrls.add(existingFileUrl);
                }
            } catch (Exception e) {
                LOGGER.severe("TTSDetail 처리 중 오류 발생: " + e.getMessage());
                throw e;
            }
        }

        LOGGER.info("convertAllTtsDetails 완료");
        return fileUrls;
    }

    private Map<String, String> processNewData(TTSDetailRequestDto detail, String projectName) {
        LOGGER.info("processNewData 호출: " + detail);
        try {
            // TTSDetail 객체 생성
            TTSDetail ttsDetail = TTSDetail.createTTSDetail(null, detail.getUnitScript(), detail.getUnitSequence());
            LOGGER.info("TTSDetail 생성 완료: " + ttsDetail);

            // 데이터 업데이트
            LOGGER.info("TTSDetail 업데이트 시작");
            ttsDetail.updateTTSDetail(
                    ttsDetailRepository.findVoiceStyleById(detail.getVoiceStyleId()),
                    detail.getUnitScript(),
                    detail.getUnitSpeed(),
                    detail.getUnitPitch(),
                    detail.getUnitVolume(),
                    detail.getUnitSequence(),
                    false
            );
            LOGGER.info("TTSDetail 업데이트 완료");

            // Google TTS API 호출
            LOGGER.info("Google TTS API 호출 시작");
            ByteString audioContent = callTTSApi(ttsDetail);
            LOGGER.info("Google TTS API 호출 완료");

            // 파일 저장
//            String filePath = OUTPUT_DIR + "new_" + detail.getUnitSequence() + ".wav"; // 기존 저장 이름
            String filePath =  OUTPUT_DIR + "tts_"+ projectName + "_" + detail.getUnitSequence() + ".wav";
            LOGGER.info("오디오 파일 저장 시작: " + filePath);
            saveAudioContent(audioContent, filePath);
            LOGGER.info("오디오 파일 저장 완료");

            return Map.of("unitSequence", String.valueOf(detail.getUnitSequence()), "fileUrl", filePath);
        } catch (Exception e) {
            LOGGER.severe("processNewData 처리 중 오류 발생: " + e.getMessage());
            throw new BusinessException(ErrorCode.NEW_DATA_PROCESSING_FAILED);
        }
    }

    private Map<String, String> processExistingData(TTSDetailRequestDto detail, String projectName) {
        LOGGER.info("processExistingData 호출: " + detail);
        try {
            // 데이터 조회
            LOGGER.info("기존 TTSDetail 조회 시작: ID = " + detail.getId());
            TTSDetail ttsDetail = ttsDetailRepository.findById(detail.getId())
                    .orElseThrow(() -> {
                        LOGGER.severe("TTSDetail ID 조회 실패: " + detail.getId());
                        throw new BusinessException(ErrorCode.TTS_DETAIL_NOT_FOUND);

                    });
            LOGGER.info("TTSDetail 조회 성공: " + ttsDetail);

            // 데이터 업데이트
            LOGGER.info("TTSDetail 업데이트 시작");
            ttsDetail.updateTTSDetail(
                    ttsDetailRepository.findVoiceStyleById(detail.getVoiceStyleId()),
                    detail.getUnitScript(),
                    detail.getUnitSpeed(),
                    detail.getUnitPitch(),
                    detail.getUnitVolume(),
                    detail.getUnitSequence(),
                    false
            );
            LOGGER.info("TTSDetail 업데이트 완료");

            // Google TTS API 호출
            LOGGER.info("Google TTS API 호출 시작");
            ByteString audioContent = callTTSApi(ttsDetail);
            LOGGER.info("Google TTS API 호출 완료");

            // 파일 저장
//            String filePath = OUTPUT_DIR + "existing_" + detail.getId() + ".wav";
            String filePath =  OUTPUT_DIR + "tts_" + projectName + "_" + detail.getUnitSequence() + ".wav";
            LOGGER.info("오디오 파일 저장 시작: " + filePath);
            saveAudioContent(audioContent, filePath);
            LOGGER.info("오디오 파일 저장 완료");

            // 데이터 저장
//            LOGGER.info("TTSDetail 데이터 저장 시작");
//            ttsDetailRepository.save(ttsDetail);
//            LOGGER.info("TTSDetail 데이터 저장 완료");

            return Map.of("unitSequence", String.valueOf(detail.getUnitSequence()), "fileUrl", filePath);
        } catch (Exception e) {
            LOGGER.severe("processExistingData 처리 중 오류 발생: " + e.getMessage());
            throw new BusinessException(ErrorCode.EXISTING_DATA_PROCESSING_FAILED);
        }
    }

    private ByteString callTTSApi(TTSDetail ttsDetail) {
        LOGGER.info("callTTSApi 호출: " + ttsDetail);
        if (ttsDetail == null) {
            LOGGER.severe("TTSDetail 객체가 null입니다.");
            throw new IllegalArgumentException("TTSDetail 객체가 null입니다.");
        }
        if (ttsDetail.getUnitScript() == null || ttsDetail.getVoiceStyle() == null) {
            LOGGER.severe("TTSDetail의 필드 값이 비어 있습니다.");
            throw new IllegalArgumentException("TTSDetail의 필드 값이 비어 있습니다.");
        }

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            LOGGER.info("Google TTS 클라이언트 생성 성공");

            SynthesisInput input = SynthesisInput.newBuilder().setText(ttsDetail.getUnitScript()).build();
            SsmlVoiceGender ssmlGender = switch (ttsDetail.getVoiceStyle().getGender().toLowerCase()) {
                case "male" -> SsmlVoiceGender.MALE;
                case "female" -> SsmlVoiceGender.FEMALE;
                default -> SsmlVoiceGender.NEUTRAL;
            };
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(ttsDetail.getVoiceStyle().getLanguageCode())
                    .setSsmlGender(ssmlGender)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSpeakingRate(ttsDetail.getUnitSpeed())
                    .setVolumeGainDb(ttsDetail.getUnitVolume())
                    .setPitch(ttsDetail.getUnitPitch())
                    .build();

            LOGGER.info("Google TTS API 요청 준비 완료");
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            LOGGER.info("Google TTS API 호출 완료");

            if (response.getAudioContent().isEmpty()) {
                LOGGER.severe("TTS 변환 실패: 응답의 오디오 콘텐츠가 비어 있습니다.");
                throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED_EMPTY_CONTENT);
            }
            LOGGER.info("TTS 변환 성공: 응답 크기 = " + response.getAudioContent().size() + " bytes");

            return response.getAudioContent();
        } catch (Exception e) {
            LOGGER.severe("Google TTS API 호출 중 오류 발생: " + e.getMessage());
            throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED);
        }
    }

    private void saveAudioContent(ByteString audioContent, String filePath) throws IOException {
        LOGGER.info("saveAudioContent 호출: 파일 저장 경로 = " + filePath);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(audioContent.toByteArray());
            LOGGER.info("오디오 콘텐츠 저장 완료: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("오디오 파일 저장 중 오류 발생: " + e.getMessage());
            throw new BusinessException(ErrorCode.AUDIO_FILE_SAVE_ERROR);
        }
    }

    private void checkTextLanguage(String text, String languageCode) {
        LOGGER.info("checkTextLanguage 호출: 텍스트 = " + text + ", 언어 코드 = " + languageCode);
        boolean isKorean = text.matches(".*[가-힣].*");
        boolean isChinese = text.matches(".*[\\u4E00-\\u9FFF].*");
        boolean isJapanese = text.matches(".*[\\u3040-\\u30FF\\u31F0-\\u31FF].*");
        boolean isEnglish = text.matches(".*[A-Za-z].*");

        switch (languageCode) {
            case "ko-KR":
                if (!isKorean) {
                    LOGGER.severe("언어 불일치: 텍스트가 한국어가 아님");
                    throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_KO_KR);
                }
                break;
            case "zh-CN":
                if (!isChinese) {
                    LOGGER.severe("언어 불일치: 텍스트가 중국어가 아님");
                    throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_ZH_CN);
                }
                break;
            case "ja-JP":
                if (!isJapanese) {
                    LOGGER.severe("언어 불일치: 텍스트가 일본어가 아님");
                    throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_JA_JP);
                }
                break;
            case "en-US":
            case "en-GB":
                if (!isEnglish) {
                    LOGGER.severe("언어 불일치: 텍스트가 영어가 아님");
                    throw new BusinessException(ErrorCode.INVALID_TEXT_FOR_EN);
                }
                break;
            default:
                LOGGER.severe("지원되지 않는 언어 코드: " + languageCode);
                throw new BusinessException(ErrorCode.UNSUPPORTED_LANGUAGE_CODE);
        }
    }


}
