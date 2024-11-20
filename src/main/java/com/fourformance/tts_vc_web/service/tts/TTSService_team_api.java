package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.common.constant.APIUnitStatusConst;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.APIStatus;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.repository.APIStatusRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
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

    private final TTSProjectRepository ttsProjectRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final APIStatusRepository apiStatusRepository;
    private final TTSService_team_multi ttsServiceTeamMulti; // 통합 서비스 호출
    private final S3Service s3Service;
    private static final Logger LOGGER = Logger.getLogger(TTSService_team_api.class.getName());

    /**
     * 모든 TTS 디테일 처리: 신규 데이터 생성 또는 기존 데이터 업데이트
     * @param ttsSaveDto TTS 프로젝트 및 디테일 데이터를 담은 DTO
     * @return 생성된 오디오 파일 경로 리스트
     */
    public List<Map<String, String>> convertAllTtsDetails(TTSSaveDto ttsSaveDto) {
        LOGGER.info("convertAllTtsDetails 호출: " + ttsSaveDto);

        // 프로젝트 저장 (새로 생성 또는 업데이트)
        TTSProject ttsProject = saveOrUpdateProject(ttsSaveDto);


        // 프로젝트의 모든 TTS 디테일 처리
        List<Map<String, String>> fileUrls = new ArrayList<>();
        for (TTSDetailDto detailDto : ttsSaveDto.getTtsDetails()) {
            // 디테일 처리 및 저장
            TTSDetail ttsDetail = saveOrUpdateDetail(detailDto, ttsProject);

            try {
                LOGGER.info("TTSDetail 처리 시작: " + detailDto);
                fileUrls.add(processTtsDetail(detailDto, ttsProject));
                LOGGER.info("TTSDetail 처리 완료: " + detailDto);
            } catch (Exception e) {
                LOGGER.severe("TTSDetail 처리 중 오류 발생: " + detailDto + ", 메시지: " + e.getMessage());
                throw new BusinessException(ErrorCode.TTS_DETAIL_PROCESSING_FAILED);
            }
        }

        LOGGER.info("convertAllTtsDetails 완료: 생성된 파일 URLs = " + fileUrls);
        return fileUrls;
    }

    /**
     * 프로젝트 저장 또는 업데이트
     * @param ttsSaveDto 프로젝트 저장 데이터를 담은 DTO
     * @return 저장된 프로젝트 엔티티
     */
    private TTSProject saveOrUpdateProject(TTSSaveDto ttsSaveDto) {
        // 프로젝트 ID 존재 여부 확인
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

        // ID를 기반으로 프로젝트 조회 및 반환
        return ttsProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TTS_PROJECT_NOT_FOUND));
    }

    /**
     * TTS 디테일 저장 또는 업데이트
     * @param detailDto TTS 디테일 데이터
     * @param ttsProject 연결된 프로젝트
     * @return 저장된 TTS 디테일 엔티티
     */
    private TTSDetail saveOrUpdateDetail(TTSDetailDto detailDto, TTSProject ttsProject) {

        if (detailDto.getId() == null) {
            detailDto.setId(ttsServiceTeamMulti.createTTSDetailCustom(detailDto, ttsProject));
        } else {
            // 기존 디테일 업데이트
            detailDto.setId(ttsServiceTeamMulti.processTTSDetailCustom(detailDto, ttsProject));
        }

        // Repository를 통해 저장된 TTSDetail 조회
        return ttsDetailRepository.findById(detailDto.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TTS_DETAIL_NOT_FOUND));
    }

    /**
     * TTS 디테일 처리: Google TTS API를 통해 텍스트를 오디오로 변환하고 저장
     * @param detailDto TTS 디테일 데이터
     * @return 변환된 오디오 파일 경로
     */
    private Map<String, String> processTtsDetail(TTSDetailDto detailDto, TTSProject ttsProject) {
        ByteString audioContent = callTTSApi(detailDto, ttsProject);
        String fileUrl = saveAudioFile(audioContent, detailDto.getUnitSequence(), ttsProject.getMember().getId(), ttsProject.getId(), detailDto.getId());
        return Map.of("fileUrl", fileUrl); // filePath 대신 fileUrl로 변경
    }

    /**
     * Google TTS API 호출: 텍스트를 오디오로 변환
     * @param detailDto TTS 디테일 데이터
     * @return 변환된 오디오 콘텐츠 (ByteString)
     */
    private ByteString callTTSApi(TTSDetailDto detailDto, TTSProject ttsProject) {

        System.out.println("detailDto = " + detailDto.toString());
        System.out.println("ttsProject = " + ttsProject.toString());
        TTSDetail ttsDetail = ttsDetailRepository.findById(detailDto.getId()).get();

        String languageCode = ttsDetailRepository.findVoiceStyleById(detailDto.getVoiceStyleId()).getLanguageCode();
        String gender = ttsDetailRepository.findVoiceStyleById(detailDto.getVoiceStyleId()).getGender();

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


        // APIStatus 생성 및 저장
        APIStatus apiStatus = APIStatus.createAPIStatus(null, ttsDetail, requestPayload);
        apiStatusRepository.save(apiStatus);

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
            checkTextLanguage(ttsDetail.getUnitScript(), ttsDetail.getVoiceStyle().getLanguageCode());

            // Google TTS API 호출
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            if (response.getAudioContent().isEmpty()) {
                apiStatus.updateResponseInfo(
                        "TTS 변환 실패: 응답의 오디오 콘텐츠가 비어 있습니다.",
                        500,
                        APIUnitStatusConst.FAILURE
                );
                apiStatusRepository.save(apiStatus);

                ttsProject.updateAPIStatus(APIStatusConst.FAILURE);
                ttsProjectRepository.save(ttsProject);

                throw new BusinessException(ErrorCode.TTS_CONVERSION_FAILED_EMPTY_CONTENT);
            }

            // 응답 페이로드 생성
            String responsePayload = String.format(
                    "{ \"audioContentSize\": %d, \"language\": \"%s\", \"gender\": \"%s\", \"speed\": %.2f, \"volume\": %.2f, \"pitch\": %.2f }",
                    response.getAudioContent().size(),
                    ttsDetail.getVoiceStyle().getLanguageCode(),
                    ttsDetail.getVoiceStyle().getGender(),
                    ttsDetail.getUnitSpeed(),
                    ttsDetail.getUnitVolume(),
                    ttsDetail.getUnitPitch()
            );

            apiStatus.updateResponseInfo(responsePayload, 200, APIUnitStatusConst.SUCCESS);
            apiStatusRepository.save(apiStatus);

            ttsProject.updateAPIStatus(APIStatusConst.SUCCESS);
            ttsProjectRepository.save(ttsProject);

            LOGGER.info("Google TTS API 호출 및 변환 성공");
            return response.getAudioContent();

        } catch (IOException e) {
            String errorPayload = "Error: " + e.getMessage();
            apiStatus.updateResponseInfo(errorPayload, 500, APIUnitStatusConst.FAILURE);
            apiStatusRepository.save(apiStatus);

            ttsProject.updateAPIStatus(APIStatusConst.FAILURE);
            ttsProjectRepository.save(ttsProject);



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

    private String saveAudioFile(ByteString audioContent, int sequence, Long userId, Long projectId, Long detailId) {
        String fileName = "tts_audio_" + sequence + ".wav";

        LOGGER.info("saveAudioFile 호출: fileName = " + fileName);

        try {
            // 임시 파일 생성
            File tempFile = File.createTempFile("tts_audio_", ".wav");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioContent.toByteArray());
            }

            LOGGER.info("임시 파일 생성 성공: " + tempFile.getAbsolutePath());

            // S3에 업로드
            MultipartFile multipartFile = convertFileToMultipartFile(tempFile, fileName);
            String s3FileUrl = s3Service.uploadUnitSaveFile(multipartFile, userId, projectId, detailId);

            // 업로드 후 임시 파일 삭제
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

    // File을 MultipartFile로 변환하는 유틸리티 메서드
    private MultipartFile convertFileToMultipartFile(File file, String fileName) throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "audio/wav";
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return java.nio.file.Files.readAllBytes(file.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                java.nio.file.Files.copy(file.toPath(), dest.toPath());
            }
        };
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
}

