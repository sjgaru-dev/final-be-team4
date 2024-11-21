package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.CommonFileUtils;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSResponseDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.repository.VoiceStyleRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

@Service
@Transactional
@RequiredArgsConstructor
public class TTSService_team_api {

    // 의존성 주입: Repository와 기타 서비스들
    private final TTSProjectRepository ttsProjectRepository; // 프로젝트 데이터에 접근하기 위한 Repository
    private final TTSDetailRepository ttsDetailRepository; // TTS 디테일 데이터에 접근하기 위한 Repository
    private final APIStatusRepository apiStatusRepository; // API 상태를 저장하고 조회하기 위한 Repository
    private final VoiceStyleRepository voiceStyleRepository;
    private final TTSService_team_multi ttsServiceTeamMulti; // 통합 서비스 호출을 위한 클래스
    private final S3Service s3Service; // S3 파일 업로드를 처리하는 서비스

    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api.class.getName()); // 로그 기록을 위한 Logger

    /**
     * 모든 TTS 디테일 처리: 데이터를 생성 또는 업데이트하고 오디오 파일을 생성.
     *
     * @param ttsSaveDto 프로젝트와 디테일 데이터를 포함한 DTO
     * @return 생성된 오디오 파일 경로 리스트
     */
    public List<TTSResponseDetailDto> convertAllTtsDetails(TTSSaveDto ttsSaveDto) {
        LOGGER.info("convertAllTtsDetails 호출: " + ttsSaveDto);

        // 프로젝트 저장 또는 업데이트
        TTSProject ttsProject = saveOrUpdateProject(ttsSaveDto);

        // 프로젝트의 TTS 디테일 데이터 처리
        List<TTSResponseDetailDto> responseDetails = new ArrayList<>();

        for (TTSDetailDto detailDto : ttsSaveDto.getTtsDetails()) {
            // 디테일 데이터 저장 또는 업데이트
            TTSDetail ttsDetail = saveOrUpdateDetail(detailDto, ttsProject);

            try {
                LOGGER.info("TTSDetail 처리 시작: " + detailDto);
                // 디테일 처리 및 오디오 파일 경로 저장

                Map<String, String> fileUrlMap = processTtsDetail(detailDto, ttsProject);
                String fileUrl = fileUrlMap.get("fileUrl");

                // TTSResponseDetailDto 생성 및 추가
                TTSResponseDetailDto responseDetail = TTSResponseDetailDto.builder()
                        .id(ttsDetail.getId())
                        .ProjectId(ttsProject.getId())
                        .unitScript(ttsDetail.getUnitScript())
                        .unitSpeed(ttsDetail.getUnitSpeed())
                        .unitPitch(ttsDetail.getUnitPitch())
                        .unitVolume(ttsDetail.getUnitVolume())
                        .isDeleted(ttsDetail.getIsDeleted())
                        .unitSequence(ttsDetail.getUnitSequence())
                        .UnitVoiceStyleId(ttsDetail.getVoiceStyle().getId())
                        .fileUrl(fileUrl) // 처리된 URL 삽입
                        .build();

                responseDetails.add(responseDetail);
                LOGGER.info("TTSDetail 처리 완료: " + detailDto);

            } catch (Exception e) {
                LOGGER.severe("TTSDetail 처리 중 오류 발생: " + detailDto + ", 메시지: " + e.getMessage());
                // 디테일 처리 실패 시 예외 발생
                throw new BusinessException(ErrorCode.TTS_DETAIL_PROCESSING_FAILED);
            }
        }

        LOGGER.info("convertAllTtsDetails 완료: 생성된 Response Details = " + responseDetails);
        return responseDetails;
    }

    /**
     * 프로젝트 저장 또는 업데이트
     *
     * @param ttsSaveDto 프로젝트 데이터를 포함한 DTO
     * @return 저장된 프로젝트 엔티티
     */
    private TTSProject saveOrUpdateProject(TTSSaveDto ttsSaveDto) {
        // 프로젝트 ID가 존재하는 경우 업데이트, 없으면 새로 생성
        Long projectId = Optional.ofNullable(ttsSaveDto.getProjectId())
                .map(id -> {
                    // 기존 프로젝트 업데이트
                    ttsServiceTeamMulti.updateProjectCustom(ttsSaveDto);
                    return id;
                })
                .orElseGet(() -> {
                    // 새로운 프로젝트 생성
                    return ttsServiceTeamMulti.createNewProjectCustom(ttsSaveDto);
                });

        // ID를 통해 프로젝트를 조회하고 반환
        return ttsProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TTS_PROJECT_NOT_FOUND));
    }

    /**
     * TTS 디테일 저장 또는 업데이트
     *
     * @param detailDto TTS 디테일 데이터를 포함한 DTO
     * @param ttsProject 연결된 프로젝트
     * @return 저장된 TTS 디테일 엔티티
     */
    private TTSDetail saveOrUpdateDetail(TTSDetailDto detailDto, TTSProject ttsProject) {
        // ID가 없으면 새로 생성, 있으면 업데이트
        if (detailDto.getId() == null) {
            detailDto.setId(ttsServiceTeamMulti.createTTSDetailCustom(detailDto, ttsProject));
        } else {
            detailDto.setId(ttsServiceTeamMulti.processTTSDetailCustom(detailDto, ttsProject));
        }

        // 저장된 디테일 데이터를 조회하고 반환
        return ttsDetailRepository.findById(detailDto.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TTS_DETAIL_NOT_FOUND));
    }

    /**
     * TTS 디테일 처리: Google TTS API를 통해 텍스트를 오디오로 변환하고 저장
     *
     * @param detailDto TTS 디테일 데이터를 포함한 DTO
     * @param ttsProject 연결된 프로젝트
     * @return 변환된 오디오 파일 경로를 포함한 Map
     */
    private Map<String, String> processTtsDetail(TTSDetailDto detailDto, TTSProject ttsProject) {
        // Google TTS API 호출로 오디오 데이터 생성
        ByteString audioContent = callTTSApi(detailDto, ttsProject);
        // 오디오 파일을 저장하고 URL 반환
        String fileUrl = saveAudioFile(audioContent, detailDto.getUnitSequence(),
                ttsProject.getMember().getId(), ttsProject.getId(), detailDto.getId());
        return Map.of("fileUrl", fileUrl);
    }

    /**
     * Google TTS API 호출: 텍스트를 오디오로 변환
     *
     * @param detailDto TTS 디테일 데이터를 포함한 DTO
     * @return 변환된 오디오 데이터(ByteString)
     */
    private ByteString callTTSApi(TTSDetailDto detailDto, TTSProject ttsProject) {
        LOGGER.info("callTTSApi 호출: " + detailDto);

        // TTS 디테일과 음성 스타일 데이터 조회
        TTSDetail ttsDetail = ttsDetailRepository.findById(ttsProject.getMember().getId()).orElseThrow();
        String languageCode = voiceStyleRepository.findById(detailDto.getUnitVoiceStyleId()).get().getLanguageCode();
        String gender = voiceStyleRepository.findById(detailDto.getUnitVoiceStyleId()).get().getGender();

        // 요청 페이로드 생성
        String requestPayload = String.format(
                "{ \"text\": \"%s\", \"language\": \"%s\", \"gender\": \"%s\", \"speed\": %.2f, \"volume\": %.2f, \"pitch\": %.2f }",
                detailDto.getUnitScript(),
                languageCode,
                gender,
                detailDto.getUnitSpeed(),
                detailDto.getUnitVolume(),
                detailDto.getUnitPitch()
        );

        // APIStatus 엔티티 생성 및 저장
        APIStatus apiStatus = APIStatus.createAPIStatus(null, ttsDetail, requestPayload);
        apiStatusRepository.save(apiStatus);

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Google TTS API 요청 생성
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(Optional.ofNullable(detailDto.getUnitScript())
                            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_UNIT_SCRIPT)))
                    .build();

            // 음성 및 오디오 설정 생성
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

            // Google TTS API 호출
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // 응답 검증 및 처리
            if (response.getAudioContent().isEmpty()) {
                throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED_EMPTY_CONTENT);
            }
            LOGGER.info("Google TTS API 호출 성공");
            return response.getAudioContent();
        } catch (IOException e) {
            LOGGER.severe("Google TTS API 호출 중 오류: " + e.getMessage());
            throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED);
        }
    }

    /**
     * VoiceStyle의 Gender를 SsmlVoiceGender로 변환
     *
     * @param detailDto TTS 디테일 데이터를 포함한 DTO
     * @return 변환된 SsmlVoiceGender
     */
    private SsmlVoiceGender getSsmlVoiceGender(TTSDetailDto detailDto) {
        // 음성 스타일의 Gender 데이터를 가져와 변환
        String gender = voiceStyleRepository.findById(detailDto.getUnitVoiceStyleId()).get().getGender();
        return switch (gender.toLowerCase()) {
            case "male" -> SsmlVoiceGender.MALE;
            case "female" -> SsmlVoiceGender.FEMALE;
            default -> SsmlVoiceGender.NEUTRAL;
        };
    }

    /**
     * 변환된 오디오 파일을 저장
     *
     * @param audioContent 변환된 오디오 데이터
     * @param sequence 유닛 시퀀스
     * @param userId 사용자 ID
     * @param projectId 프로젝트 ID
     * @param detailId 디테일 ID
     * @return 저장된 오디오 파일 URL
     */
    private String saveAudioFile(ByteString audioContent, int sequence, Long userId, Long projectId, Long detailId) {
        String fileName = "tts_audio_" + sequence + ".wav";
        LOGGER.info("saveAudioFile 호출: fileName = " + fileName);

        try {
            // 임시 파일 생성 및 저장
            File tempFile = File.createTempFile("tts_audio_", ".wav");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioContent.toByteArray());
            }

            // S3에 업로드
            MultipartFile multipartFile = CommonFileUtils.convertFileToMultipartFile(tempFile, fileName);
            String s3FileUrl = s3Service.uploadUnitSaveFile(multipartFile, userId, projectId, detailId);

            // 임시 파일 삭제
            if (!tempFile.delete()) {
                LOGGER.warning("임시 파일 삭제 실패: " + tempFile.getAbsolutePath());
            }

            LOGGER.info("S3 업로드 성공: " + s3FileUrl);
            return s3FileUrl;
        } catch (IOException e) {
            LOGGER.severe("saveAudioFile 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.AUDIO_FILE_SAVE_ERROR);
        }
    }

    /**
     * 텍스트와 언어 코드의 일치 여부를 검증
     *
     * @param text 텍스트 데이터
     * @param languageCode 언어 코드
     */
    private void checkTextLanguage(String text, String languageCode) {
        boolean isKorean = text.matches(".*[가-힣].*");
        boolean isChinese = text.matches(".*[\\u4E00-\\u9FFF].*");
        boolean isJapanese = text.matches(".*[\\u3040-\\u30FF\\u31F0-\\u31FF].*");
        boolean isEnglish = text.matches(".*[A-Za-z].*");

        // 언어 코드와 텍스트의 일치 여부 확인
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
}

