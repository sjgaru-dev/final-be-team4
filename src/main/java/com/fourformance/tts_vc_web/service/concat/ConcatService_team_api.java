package com.fourformance.tts_vc_web.service.concat;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.dto.concat.*;
import com.fourformance.tts_vc_web.repository.ConcatDetailRepository;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;




@Service
@Transactional
@RequiredArgsConstructor
public class ConcatService_team_api {

    private final ConcatService_team_aws ConcatServiceTeamAws;
    private final S3Service s3Service; // S3 파일 업로드를 처리하는 서비스
    private final AudioProcessingService audioProcessingService;

    private final ConcatProjectRepository concatProjectRepository;
    private final ConcatDetailRepository concatDetailRepository;
    private final MemberRepository memberRepository;

    private static final Logger LOGGER = Logger.getLogger(ConcatService_team_api.class.getName());
    private String uploadDir;

    @PostConstruct
    public void initialize() {
        // 업로드 디렉토리 설정
        uploadDir = System.getProperty("user.home") + "/uploads";
        File uploadFolder = new File(uploadDir);

        if (!uploadFolder.exists()) {
            if (uploadFolder.mkdirs()) {
                LOGGER.info("업로드 디렉토리가 생성되었습니다: " + uploadDir);
            } else {
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir);
            }
        }
    }

//    public String convertMultipleAudios(MultipartFile[] sourceAudios) {
//        List<String> savedFilePaths = new ArrayList<>();
//        String silenceFilePath = null;
//
//        try {
//            // 파일 저장
//            for (MultipartFile audio : sourceAudios) {
//                if (!audio.isEmpty()) {
//                    String savedFileName = UUID.randomUUID().toString() + "_" + audio.getOriginalFilename();
//                    File savedFile = new File(uploadDir, savedFileName);
//                    audio.transferTo(savedFile);
//                    savedFilePaths.add(savedFile.getAbsolutePath());
//                    LOGGER.info("파일 저장 완료: " + savedFile.getAbsolutePath());
//                }
//            }
//
//            if (savedFilePaths.isEmpty()) {
//                throw new RuntimeException("업로드된 파일이 없습니다.");
//            }
//
//            // 병합된 파일 저장 경로
//            String mergedFilePath = uploadDir + "/merged_" + UUID.randomUUID() + ".mp3";
//
//            // FFmpeg로 오디오 병합
//            silenceFilePath = createSilenceFile(2); // 2초 무음 파일 생성
//            mergeAudioFilesWithSilence(savedFilePaths, mergedFilePath, silenceFilePath);
//
//            LOGGER.info("병합된 파일 저장 경로: " + mergedFilePath);
//
//            return mergedFilePath;
//
//        } catch (Exception e) {
//            LOGGER.severe("오디오 병합 실패: " + e.getMessage());
//            throw new RuntimeException("오디오 병합 중 오류 발생", e);
//
//        } finally {
//            // 파일 정리
//            deleteFiles(savedFilePaths);
//            if (silenceFilePath != null) {
//                deleteFiles(List.of(silenceFilePath));
//            }
//        }
//    }

//    private String createSilenceFile(int silenceDurationSec) throws IOException {
//        String silenceFilePath = uploadDir + "/temp_silence.mp3";
//
//        FFmpeg ffmpeg = new FFmpeg("/opt/homebrew/bin/ffmpeg"); // FFmpeg 경로 설정
//        FFmpegBuilder silenceBuilder = new FFmpegBuilder()
//                .setInput("anullsrc")
//                .addExtraArgs("-f", "lavfi")
//                .overrideOutputFiles(true)
//                .addOutput(silenceFilePath)
//                .setAudioCodec("libmp3lame")
//                .setAudioChannels(2)
//                .setAudioSampleRate(44100)
//                .setDuration(silenceDurationSec, TimeUnit.SECONDS)
//                .done();
//
//        new FFmpegExecutor(ffmpeg).createJob(silenceBuilder).run();
//
//        LOGGER.info("무음 파일 생성 완료: " + silenceFilePath);
//        return silenceFilePath;
//    }

    private void mergeAudioFilesWithSilence(List<String> audioPaths, String outputPath, String silenceFilePath) throws IOException {
        FFmpeg ffmpeg = new FFmpeg("/opt/homebrew/bin/ffmpeg");

        // concat 필터를 위한 입력 설정
        StringBuilder filterComplexBuilder = new StringBuilder();
        List<String> inputs = new ArrayList<>();

        int inputIndex = 0;
        for (String audioPath : audioPaths) {
            inputs.add(audioPath);
            filterComplexBuilder.append("[").append(inputIndex++).append(":a]");
            if (!audioPath.equals(audioPaths.get(audioPaths.size() - 1))) {
                inputs.add(silenceFilePath);
                filterComplexBuilder.append("[").append(inputIndex++).append(":a]");
            }
        }
        filterComplexBuilder.append("concat=n=").append(inputIndex).append(":v=0:a=1[out]");

        FFmpegBuilder mergeBuilder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setAudioCodec("libmp3lame")
                .setAudioChannels(2)
                .setAudioBitRate(192000)
                .addExtraArgs("-filter_complex", filterComplexBuilder.toString())
                .addExtraArgs("-map", "[out]")
                .done();

        for (String input : inputs) {
            mergeBuilder.addInput(input);
        }

        new FFmpegExecutor(ffmpeg).createJob(mergeBuilder).run();
        LOGGER.info("오디오 병합 완료: " + outputPath);
    }

//    private void deleteFiles(List<String> filePaths) {
//        for (String filePath : filePaths) {
//            try {
//                Files.deleteIfExists(Paths.get(filePath));
//                LOGGER.info("삭제된 파일: " + filePath);
//            } catch (IOException e) {
//                LOGGER.warning("파일 삭제 실패: " + filePath + " - " + e.getMessage());
//            }
//        }
//    }

    public ConcatResponseDto convertAllConcatDetails(ConcatRequestDto concatRequestDto){
        LOGGER.info("convertAllConcatDetails 호출: " + concatRequestDto);

        // 프로젝트 저장 또는 업데이트
        ConcatProject concatProject = saveOrUpdateProject(concatRequestDto);

        // 프로젝트 ConcatResponseDto 선언
        ConcatResponseDto concatResponseDto = ConcatResponseDto.builder()
                .projectId(concatProject.getId())
                .projectName(concatProject.getProjectName())
                .globalFrontSilenceLength(concatProject.getGlobalFrontSilenceLength())
                .globalTotalSilenceLength(concatRequestDto.getGlobalTotalSilenceLength())
                .build();


        // 프로젝트 CONCAT 디테일 데이터 처리
        List<ConcatResponseDetailDto> responseDetails = new ArrayList<>();

        // 프로젝트 OUTPUT 오디오 파일 데이터 처리
        List<String> outputConcatAudios = new ArrayList<>();


        for(ConcatRequestDetailDto detailDto : concatRequestDto.getConcatRequestDetails()) {

            //디테일 데이터 저장 또는 업데이트
            ConcatDetail concatDetail = saveOrUpdateDetail(detailDto, concatProject);

            try {
                LOGGER.info("ConcatDetail 처리 시작: " + detailDto);
                // 디테일 처리 및 오디오 파일 경로 저장

                String fileUrl = uploadConcatDetailSourceAudio(detailDto, concatProject);

                // ConcatResponseDetailDto 생성 및 추가
                ConcatResponseDetailDto responseDetailDto = ConcatResponseDetailDto.builder()
                        .id(concatDetail.getId())
                        .audioSeq(concatDetail.getAudioSeq())
                        .isChecked(true)
                        .unitScript(concatDetail.getUnitScript())
                        .endSilence(concatDetail.getEndSilence())
                        .audioUrl(fileUrl)
                        .sourceAudio(detailDto.getSourceAudio())
                        .build();

                responseDetails.add(responseDetailDto);

                // OutputConcatAudioDto 생성 및 추가
//                OutputConcatAudioDto outputConcatAudioDto = OutputConcatAudioDto.builder()
//                        .generatedAudioMetaId(0L)
//                        .concatProjectId(concatProject.getId())
//                        .projectType(CONCAT)
//                        .audioUrl("")
//                        .audioFormat(AudioFormat.MP3)
//                        .build();
//                responseOutputAudios.add(outputConcatAudioDto);

                LOGGER.info("TTSDetail 처리 완료: " + detailDto);

            } catch (Exception e) {
                LOGGER.severe("TTSDetail 처리 중 오류 발생: " + detailDto + ", 메시지: " + e.getMessage());
                // 디테일 처리 실패 시 예외 발생
                throw new BusinessException(ErrorCode.TTS_DETAIL_PROCESSING_FAILED);
            }
        }

        // 여기까지 concat 프로젝트 및 concat 디테일 저장완료
        // 체크된 것만 concat 병합처리
//
//         List<ConcatResponseDetailDto> checkedConcatRequestDetailDto = new ArrayList<>();


        String fileUrl = mergeAudioFilesAndUploadToS3(responseDetails, uploadDir, concatRequestDto.getMemberId(), concatProject.getId());

        outputConcatAudios.add(fileUrl);

        concatResponseDto.setOutputConcatAudios(outputConcatAudios);


        return concatResponseDto;
    }

    /**
     * 병합된 오디오 파일을 생성하고 S3에 업로드한 뒤, URL을 반환합니다.
     */
    public String mergeAudioFilesAndUploadToS3(List<ConcatResponseDetailDto> audioDetails, String uploadDir, Long userId, Long projectId) {
        List<String> savedFilePaths = new ArrayList<>();
        List<String> silenceFilePaths = new ArrayList<>();
        String mergedFilePath = null;

        try {
            // 1. 체크된 파일만 병합 대상에 포함
            List<ConcatResponseDetailDto> filteredDetails = audioDetails.stream()
                    .filter(ConcatResponseDetailDto::isChecked)
                    .collect(Collectors.toList());

            if (filteredDetails.isEmpty()) {
                LOGGER.severe("병합할 파일이 없습니다: audioDetails=" + audioDetails);
                throw new RuntimeException("병합할 파일이 없습니다.");
            }

            // 2. 파일 다운로드 및 침묵 파일 생성
            for (ConcatResponseDetailDto detail : filteredDetails) {
                if (detail.getAudioUrl() != null && !detail.getAudioUrl().isEmpty()) {
                    try {
                        // 로컬에 저장할 파일 경로 설정
                        String savedFileName = UUID.randomUUID() + "_audioSeq_" + detail.getAudioSeq() + ".wav";
                        Path savedFilePath = Paths.get(uploadDir, savedFileName);

                        // S3에서 파일 다운로드
                        s3Service.downloadFileFromS3(detail.getAudioUrl(), savedFilePath.toString());
                        savedFilePaths.add(savedFilePath.toString());

                        // 침묵 파일 생성
                        String silenceFilePath = audioProcessingService.createSilenceFile(detail.getEndSilence().longValue(), uploadDir);
                        if (silenceFilePath != null) {
                            silenceFilePaths.add(silenceFilePath);
                        }
                    } catch (Exception e) {
                        LOGGER.severe("파일 다운로드 중 오류 발생: audioUrl=" + detail.getAudioUrl() + ", error=" + e.getMessage());
                        throw new RuntimeException("파일 다운로드 중 오류 발생", e);
                    }
                } else {
                    LOGGER.warning("유효하지 않은 audioUrl: " + detail.getAudioUrl());
                }
            }

            // 3. 병합된 파일 생성
            mergedFilePath = audioProcessingService.mergeAudioFilesWithSilence(savedFilePaths, silenceFilePaths, uploadDir);

            // 4. S3에 업로드
            MultipartFile mergedFile = audioProcessingService.convertToMultipartFile(mergedFilePath);
            String mergedFileUrl = s3Service.uploadConcatSaveFile(mergedFile, userId, projectId);

            return mergedFileUrl;

        } catch (Exception e) {
            throw new RuntimeException("오디오 병합 및 S3 업로드 중 오류 발생", e);

        } finally {
            // 5. 임시 파일 정리
            audioProcessingService.deleteFiles(savedFilePaths);
            audioProcessingService.deleteFiles(silenceFilePaths);
            if (mergedFilePath != null) {
                audioProcessingService.deleteFiles(List.of(mergedFilePath));
            }
        }
    }



    private String uploadConcatDetailSourceAudio(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
//        // Google TTS API 호출로 오디오 데이터 생성
//        ByteString audioContent = callTTSApi(detailDto, ttsProject);
//        // 오디오 파일을 저장하고 URL 반환
//        String fileUrl = saveAudioFile(audioContent, detailDto.getUnitSequence(),
//                ttsProject.getMember().getId(), ttsProject.getId(), detailDto.getId());

      List<MultipartFile> files = new ArrayList<>();
      files.add(detailDto.getSourceAudio());

        // S3 파일을 저장
        List<String> fileUrls = s3Service.uploadAndSaveMemberFile(files, concatProject.getMember().getId(), concatProject.getId(), AudioType.CONCAT, null) ;
        return fileUrls.get(0);
    }

    /**
     * 프로젝트 저장 또는 업데이트
     *
     * @param concatRequestDto 프로젝트 데이터를 포함한 DTO
     * @return 저장된 프로젝트 엔티티
     */
    private ConcatProject saveOrUpdateProject(ConcatRequestDto concatRequestDto) {
        // 프로젝트 ID가 존재하는 경우 업데이트, 없으면 새로 생성
        Long projectId = Optional.ofNullable(concatRequestDto.getProjectId())
                .map(id -> {
                    // 기존 프로젝트 업데이트
                    updateProject(concatRequestDto);
                    return id;
                })
                .orElseGet(() -> {
                    // 새로운 프로젝트 생성
                    return createNewProject(concatRequestDto);
                });

        // ID를 통해 프로젝트를 조회하고 반환
        return concatProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TTS_PROJECT_NOT_FOUND));
    }

//    /**
//     * 디테일 저장 또는 업데이트
//     *
//     * @param detailDto TTS 디테일 데이터를 포함한 DTO
//     * @param ttsProject 연결된 프로젝트
//     * @return 저장된 TTS 디테일 엔티티
//     */
    private ConcatDetail saveOrUpdateDetail(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
        // ID가 없으면 새로 생성, 있으면 업데이트
        if (detailDto.getId() == null) {
            detailDto.setId(createConcatDetail(detailDto, concatProject));
        } else {
            detailDto.setId(updateConcatDetail(detailDto, concatProject));
        }

        // 저장된 디테일 데이터를 조회하고 반환
        return concatDetailRepository.findById(detailDto.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TTS_DETAIL_NOT_FOUND));
    }

    // Concat 프로젝트 생성
    @Transactional
    public Long createNewProject(ConcatRequestDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ConcatProject concatProject = concatProjectRepository.save(
                ConcatProject.createConcatProject(member, dto.getProjectName())
        );

        return concatProject.getId();
    }

    // Concat 프로젝트 업데이트
    @Transactional
    public Long updateProject(ConcatRequestDto dto) {
        ConcatProject concatProject = concatProjectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        if (!concatProject.getMember().getId().equals(dto.getMemberId())) {
            throw new BusinessException(ErrorCode.MEMBER_PROJECT_NOT_MATCH);
        }

        concatProject.updateConcatProject(
                dto.getProjectName(),
                dto.getGlobalFrontSilenceLength(),
                dto.getGlobalTotalSilenceLength()
        );

        return concatProject.getId();
    }

    // 디테일 생성
    private Long createConcatDetail(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
        return concatDetailRepository.save(
                ConcatDetail.createConcatDetail(
                        concatProject,
                        detailDto.getAudioSeq(),
                        detailDto.isChecked(),
                        detailDto.getUnitScript(),
                        detailDto.getEndSilence()
                )
        ).getId();
    }

    // 디테일 업데이트
    private Long updateConcatDetail(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
        ConcatDetail concatDetail = concatDetailRepository.findById(detailDto.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL));

        if (!concatDetail.getConcatProject().getId().equals(concatProject.getId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL);
        }

        concatDetail.updateDetails(
                detailDto.getAudioSeq(),
                detailDto.isChecked(),
                detailDto.getUnitScript(),
                detailDto.getEndSilence(),
                false
        );

        return concatDetailRepository.save(concatDetail).getId();
    }

}
