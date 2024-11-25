package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.ConvertedMultipartFile_team_api;
import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.dto.vc.*;
import com.fourformance.tts_vc_web.repository.*;
import com.fourformance.tts_vc_web.service.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCService_team_api.class.getName());

    // Dependencies
    private final ElevenLabsClient_team_api elevenLabsClient;
    private final S3Service s3Service;
    private final MemberRepository memberRepository;
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final OutputAudioMetaRepository outputAudioMetaRepository;
    private final VCService_team_multi vcService;

    /**
     * VC 프로젝트 오디오 변환 메서드
     */
    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
        LOGGER.info("VC 프로젝트 처리 시작");

        // Step 1: 멤버 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // Step 2: VC 프로젝트 저장 및 ID 반환
        Long projectId = vcService.saveVCProject(vcSaveDto, files, member);
        if (projectId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // Step 3: 프로젝트 ID로 VC 상세 정보 반환
        List<VCDetail> vcDetails = vcDetailRepository.findByVcProject_Id(projectId);

        // Step 4: VCDetail -> VCDetailDto 변환 (localFileName 포함)
        List<VCDetailDto> vcDetailDtos = vcDetails.stream()
                .filter(vcDetail -> vcDetail.getIsChecked() && !vcDetail.getIsDeleted())
                .map(VCDetailDto::createVCDetailDtoWithLocalFileName)
                .collect(Collectors.toList());

        // Step 5: 저장된 target 오디오 ID 찾기
        MemberAudioMeta memberAudio = memberAudioMetaRepository.findSelectedAudioByTypeAndMember(AudioType.VC_TRG, memberId);

        // Step 6: target 오디오의 목소리 ID 생성
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), memberAudio);
        LOGGER.info("기존 voiceId와 동일 여부 확인: " + voiceId);

        // Step 7: src 오디오에 target 오디오 적용
        List<VCDetailResDto> vcDetailsRes = processSourceFiles(files, vcDetailDtos, voiceId, memberId);

        return vcDetailsRes;
    }

    /**
     * target 오디오 파일을 처리하여 Voice ID 생성
     */
    private String processTargetFiles(List<TrgAudioFileDto> trgFiles, MemberAudioMeta memberAudio) {
        if (trgFiles == null || trgFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
        try {
            // 하드코딩된 Voice ID 사용
            String voiceId = "U179o4j7jr5TWWnU3DJy"; // 하드코딩된 보이스 ID
            LOGGER.info("Using hardcoded Voice ID: " + voiceId);

            // Voice ID를 MemberAudioMeta에 업데이트
            memberAudio.update(voiceId);
            memberAudioMetaRepository.save(memberAudio);
            LOGGER.info("Updated MemberAudioMeta with hardcoded Voice ID: " + memberAudio);

            return voiceId;
        } catch (Exception e) {
            LOGGER.severe("Error during target file processing: " + e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

//        try {
//            // Step 1: Target 파일 URL 확인
//            if (memberAudio == null || memberAudio.getAudioUrl() == null) {
//                throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
//            }
//            String targetFileUrl = memberAudio.getAudioUrl();
//            LOGGER.info("Uploading target audio file: " + targetFileUrl);
//
//            // Step 2: Voice ID 생성
//            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);
//            LOGGER.info("Generated voiceId: " + voiceId + " for target audio: " + targetFileUrl);
//
//            // Step 3: Voice ID 저장
//            memberAudio.update(voiceId);
//            memberAudioMetaRepository.save(memberAudio);
//
//            return voiceId;
//
//        } catch (IOException e) {
//            LOGGER.severe("Error during voice ID creation: " + e.getMessage());
//            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
//        }
    }

    /**
     * src 파일 처리 및 변환
     */
    private List<VCDetailResDto> processSourceFiles(
            List<MultipartFile> inputFiles,
            List<VCDetailDto> srcFiles,
            String voiceId,
            Long memberId) {
        return srcFiles.stream()
                .map(srcFile -> {
                    // Step 1: 소스 파일 매칭
                    MultipartFile matchingFile = findMultipartFileByName(inputFiles, srcFile.getLocalFileName());
                    LOGGER.info("Matching file found: " + (matchingFile != null ? matchingFile.getOriginalFilename() : "null"));

                    // Step 2: 변환 처리
                    if (matchingFile != null) {
                        return processSingleSourceFile(srcFile, matchingFile, voiceId, memberId);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 단일 소스 파일 처리
     */
    private VCDetailResDto processSingleSourceFile(
            VCDetailDto srcFile,
            MultipartFile originFile,
            String voiceId,
            Long memberId) {
        try {
            // Step 1: 소스 파일 URL 가져오기
            String sourceFileUrl = memberAudioMetaRepository.findAudioUrlsByAudioMetaIds(
                    srcFile.getMemberAudioMetaId(),
                    AudioType.VC_SRC
            );
            LOGGER.info("Source File URL: " + sourceFileUrl);

            // Step 2: 변환 작업 수행
            String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);
            LOGGER.info("Converted file path: " + convertedFilePath);

            // Step 3: 변환된 파일 읽기
            byte[] convertedFileBytes = Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/uploads/" + convertedFilePath));
            MultipartFile convertedMultipartFile = new ConvertedMultipartFile_team_api(
                    convertedFileBytes,
                    convertedFilePath,
                    "audio/mpeg"
            );

            // Step 4: 변환된 파일을 S3에 저장
            String vcOutputUrl = s3Service.uploadUnitSaveFile(
                    convertedMultipartFile,
                    memberId,
                    srcFile.getProjectId(),
                    srcFile.getId()
            );
            LOGGER.info("Generated output file URL: " + vcOutputUrl);

            // Step 5: 결과 DTO 생성 및 반환
            return new VCDetailResDto(
                    srcFile.getId(),
                    srcFile.getProjectId(),
                    srcFile.getIsChecked(),
                    srcFile.getUnitScript(),
                    sourceFileUrl,
                    List.of(vcOutputUrl)
            );

        } catch (Exception e) {
            LOGGER.severe("Error processing single source file: " + e.getMessage());
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }

    /**
     * 로컬에서 업로드한 파일 정보 찾기
     */
    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        if (fileName == null) {
            LOGGER.warning("File name is null. Skipping search.");
            return null;
        }

        String simpleFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(simpleFileName))
                .findFirst()
                .orElse(null);
    }
}
