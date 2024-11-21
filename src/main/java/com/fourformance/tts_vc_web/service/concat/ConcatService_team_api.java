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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ConcatService_team_api {

    // 서비스 의존성 주입
    private final S3Service s3Service; // S3 연동 서비스
    private final AudioProcessingService audioProcessingService; // 오디오 처리 서비스
    private final ConcatProjectRepository concatProjectRepository; // 프로젝트 관련 저장소
    private final ConcatDetailRepository concatDetailRepository; // 디테일 관련 저장소
    private final MemberRepository memberRepository; // 멤버 관련 저장소

    private static final Logger LOGGER = Logger.getLogger(ConcatService_team_api.class.getName());
    private String uploadDir; // 업로드 디렉토리 경로

    /**
     * 서비스 초기화 메서드: 업로드 디렉토리를 생성합니다.
     */
    @PostConstruct
    public void initialize() {
        uploadDir = System.getProperty("user.home") + "/uploads";
        File uploadFolder = new File(uploadDir);

        // 디렉토리 존재 여부 확인 후 생성
        if (!uploadFolder.exists()) {
            if (!uploadFolder.mkdirs()) {
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir);
            }
        }
        LOGGER.info("업로드 디렉토리가 설정되었습니다: " + uploadDir);
    }

    /**
     * 오디오 파일 병합 프로세스 수행
     *
     * @param concatRequestDto 요청 데이터 DTO
     * @return 병합 결과 DTO
     */
    public ConcatResponseDto convertAllConcatDetails(ConcatRequestDto concatRequestDto) {
        LOGGER.info("convertAllConcatDetails 호출: " + concatRequestDto);

        // 1. 프로젝트 생성 또는 업데이트
        ConcatProject concatProject = saveOrUpdateProject(concatRequestDto);

        // 2. 응답 DTO 생성 및 초기화
        ConcatResponseDto concatResponseDto = ConcatResponseDto.builder()
                .projectId(concatProject.getId())
                .projectName(concatProject.getProjectName())
                .globalFrontSilenceLength(concatProject.getGlobalFrontSilenceLength())
                .globalTotalSilenceLength(concatRequestDto.getGlobalTotalSilenceLength())
                .build();

        List<ConcatResponseDetailDto> responseDetails = new ArrayList<>();
        List<String> outputConcatAudios = new ArrayList<>();

        // 3. 각 요청 디테일 처리
        for (ConcatRequestDetailDto detailDto : concatRequestDto.getConcatRequestDetails()) {
            // 디테일 저장 또는 업데이트
            ConcatDetail concatDetail = saveOrUpdateDetail(detailDto, concatProject);

            try {
                LOGGER.info("ConcatDetail 처리 시작: " + detailDto);

                // S3 업로드 후 파일 URL 반환
                String fileUrl = uploadConcatDetailSourceAudio(detailDto, concatProject);

                // 응답 디테일 생성 및 추가
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

                LOGGER.info("ConcatDetail 처리 완료: " + detailDto);

            } catch (Exception e) {
                LOGGER.severe("ConcatDetail 처리 중 오류 발생: " + detailDto + ", 메시지: " + e.getMessage());
                throw new BusinessException(ErrorCode.TTS_DETAIL_PROCESSING_FAILED);
            }
        }

        // 4. 병합된 오디오 생성 및 S3 업로드
        String mergedFileUrl = mergeAudioFilesAndUploadToS3(responseDetails, uploadDir, concatRequestDto.getMemberId(), concatProject.getId());
        outputConcatAudios.add(mergedFileUrl);
        concatResponseDto.setOutputConcatAudios(outputConcatAudios);

        return concatResponseDto;
    }

    /**
     * 오디오 파일 병합 및 S3 업로드
     *
     * @param audioDetails 병합 대상 디테일 리스트
     * @param uploadDir    업로드 디렉토리 경로
     * @param userId       사용자 ID
     * @param projectId    프로젝트 ID
     * @return 업로드된 병합 파일의 URL
     */
    public String mergeAudioFilesAndUploadToS3(List<ConcatResponseDetailDto> audioDetails, String uploadDir, Long userId, Long projectId) {
        List<String> savedFilePaths = new ArrayList<>();
        List<String> silenceFilePaths = new ArrayList<>();
        String mergedFilePath = null;

        try {
            // 1. 체크된 파일 필터링
            List<ConcatResponseDetailDto> filteredDetails = audioDetails.stream()
                    .filter(ConcatResponseDetailDto::isChecked)
                    .collect(Collectors.toList());

            if (filteredDetails.isEmpty()) {
                LOGGER.severe("병합할 파일이 없습니다.");
                throw new RuntimeException("병합할 파일이 없습니다.");
            }

            // 2. S3에서 파일 다운로드 및 침묵 파일 생성
            for (ConcatResponseDetailDto detail : filteredDetails) {
                if (detail.getAudioUrl() != null) {
                    String savedFilePath = s3Service.downloadFileFromS3(detail.getAudioUrl(), uploadDir);
                    savedFilePaths.add(savedFilePath);

                    String silenceFilePath = audioProcessingService.createSilenceFile(detail.getEndSilence().longValue(), uploadDir);
                    if (silenceFilePath != null) silenceFilePaths.add(silenceFilePath);
                }
            }

            // 3. 병합된 파일 생성
            mergedFilePath = audioProcessingService.mergeAudioFilesWithSilence(savedFilePaths, silenceFilePaths, uploadDir);

            // 4. 병합된 파일을 S3에 업로드 후 URL 반환
            return s3Service.uploadConcatSaveFile(audioProcessingService.convertToMultipartFile(mergedFilePath), userId, projectId);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 5. 임시 파일 삭제
            audioProcessingService.deleteFiles(savedFilePaths);
            audioProcessingService.deleteFiles(silenceFilePaths);
            if (mergedFilePath != null) {
                audioProcessingService.deleteFiles(List.of(mergedFilePath));
            }
        }
    }

    /**
     * 프로젝트 생성 또는 업데이트
     *
     * @param concatRequestDto 요청 데이터
     * @return 프로젝트 엔티티
     */
    private ConcatProject saveOrUpdateProject(ConcatRequestDto concatRequestDto) {
        return Optional.ofNullable(concatRequestDto.getProjectId())
                .map(projectId -> {
                    updateProject(concatRequestDto);
                    return concatProjectRepository.findById(projectId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.TTS_PROJECT_NOT_FOUND));
                })
                .orElseGet(() -> createNewProject(concatRequestDto));
    }

    /**
     * 새로운 프로젝트 생성
     */
    private ConcatProject createNewProject(ConcatRequestDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ConcatProject concatProject = ConcatProject.createConcatProject(member, dto.getProjectName());
        return concatProjectRepository.save(concatProject);
    }

    /**
     * 기존 프로젝트 업데이트
     */
    private void updateProject(ConcatRequestDto dto) {
        ConcatProject project = concatProjectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        if (!project.getMember().getId().equals(dto.getMemberId())) {
            throw new BusinessException(ErrorCode.MEMBER_PROJECT_NOT_MATCH);
        }

        project.updateConcatProject(dto.getProjectName(), dto.getGlobalFrontSilenceLength(), dto.getGlobalTotalSilenceLength());
    }

    /**
     * 디테일 저장 또는 업데이트
     */
    private ConcatDetail saveOrUpdateDetail(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
        return Optional.ofNullable(detailDto.getId())
                .map(id -> {
                    updateConcatDetail(detailDto, concatProject);
                    return concatDetailRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL));
                })
                .orElseGet(() -> {
                    return concatDetailRepository.save(
                            ConcatDetail.createConcatDetail(
                                    concatProject,
                                    detailDto.getAudioSeq(),
                                    detailDto.isChecked(),
                                    detailDto.getUnitScript(),
                                    detailDto.getEndSilence()
                            )
                    );
                });
    }

    /**
     * 기존 디테일 업데이트
     */
    private void updateConcatDetail(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
        ConcatDetail concatDetail = concatDetailRepository.findById(detailDto.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL));

        if (!concatDetail.getConcatProject().getId().equals(concatProject.getId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL);
        }

        concatDetail.updateDetails(detailDto.getAudioSeq(), detailDto.isChecked(), detailDto.getUnitScript(), detailDto.getEndSilence(), false);
    }

    /**
     * 요청된 디테일의 소스 오디오를 S3에 업로드 후 URL 반환
     */
    private String uploadConcatDetailSourceAudio(ConcatRequestDetailDto detailDto, ConcatProject concatProject) {
        return s3Service.uploadAndSaveMemberFile(
                List.of(detailDto.getSourceAudio()),
                concatProject.getMember().getId(),
                concatProject.getId(),
                AudioType.CONCAT,
                null
        ).get(0);
    }
}
