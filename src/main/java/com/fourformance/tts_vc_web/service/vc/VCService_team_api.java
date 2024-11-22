package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.CustomMultipartFile_team_api;
import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCService_team_api.class.getName());

    private final ElevenLabsClient_team_api elevenLabsClient;
    private final S3Service s3Service;
    private final MemberRepository memberRepository;
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final OutputAudioMetaRepository outputAudioMetaRepository;

    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
        LOGGER.info("VC 프로젝트 처리 시작");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    LOGGER.severe("멤버 확인 실패: memberId = " + memberId);
                    return new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
                });
        LOGGER.info("멤버 확인 완료: memberId = " + memberId);

        Long projectId = saveOrUpdateProject(vcSaveDto, member);
        LOGGER.info("프로젝트 저장/업데이트 완료: projectId = " + projectId);

        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files, memberId, projectId);
        LOGGER.info("타겟 파일 처리 완료: Voice ID = " + voiceId);

        List<VCDetailResDto> vcDetails = processSourceFiles(vcSaveDto.getSrcFiles(), files, voiceId, projectId, memberId);
        LOGGER.info("소스 파일 처리 완료");

        LOGGER.info("VC 프로젝트 처리 완료");
        return vcDetails;
    }

    private Long saveOrUpdateProject(VCSaveDto vcSaveDto, Member member) {
        if (vcSaveDto.getProjectId() == null) {
            VCProject vcProject = VCProject.createVCProject(member, vcSaveDto.getProjectName());
            vcProjectRepository.save(vcProject);
            LOGGER.info("새 프로젝트 생성 완료: projectId = " + vcProject.getId());
            return vcProject.getId();
        } else {
            VCProject vcProject = vcProjectRepository.findById(vcSaveDto.getProjectId())
                    .orElseThrow(() -> {
                        LOGGER.severe("프로젝트 업데이트 실패: projectId = " + vcSaveDto.getProjectId());
                        return new BusinessException(ErrorCode.NOT_EXISTS_PROJECT);
                    });
            vcProject.updateVCProject(vcSaveDto.getProjectName(), null);
            LOGGER.info("기존 프로젝트 업데이트 완료: projectId = " + vcSaveDto.getProjectId());
            return vcSaveDto.getProjectId();
        }
    }

    private String processTargetFiles(List<AudioFileDto> trgFiles, List<MultipartFile> files, Long memberId, Long projectId) {
        if (trgFiles == null || trgFiles.isEmpty()) {
            LOGGER.warning("타겟 파일이 없습니다.");
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        MultipartFile file = findMultipartFileByName(files, trgFiles.get(0).getLocalFileName());
        if (file == null) {
            LOGGER.severe("타겟 파일 로드 실패: " + trgFiles.get(0).getLocalFileName());
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        try {
            String targetFileUrl = uploadFileToS3(file, memberId, projectId, AudioType.VC_TRG);
            LOGGER.info("타겟 파일 S3 업로드 완료: " + targetFileUrl);

            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);
            LOGGER.info("Voice ID 생성 완료: " + voiceId);

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                    member,
                    generateBucketRoute(targetFileUrl),
                    targetFileUrl,
                    AudioType.VC_TRG,
                    voiceId
            );
            memberAudioMetaRepository.save(memberAudioMeta);
            LOGGER.info("MemberAudioMeta 저장 완료: ID = " + memberAudioMeta.getId());

            VCProject vcProject = vcProjectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));
            vcProject.updateVCProject(vcProject.getProjectName(), memberAudioMeta);
            vcProject.updateTrgVoiceId(voiceId);
            vcProjectRepository.save(vcProject);
            LOGGER.info("VCProject 업데이트 완료");

            return voiceId;

        } catch (IOException e) {
            LOGGER.severe("타겟 파일 처리 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    private List<VCDetailResDto> processSourceFiles(List<AudioFileDto> srcFiles, List<MultipartFile> files, String voiceId, Long projectId, Long memberId) {
        if (srcFiles == null || srcFiles.isEmpty()) {
            LOGGER.warning("소스 파일이 없습니다.");
            return List.of();
        }

        return srcFiles.stream()
                .map(srcFile -> {
                    try {
                        String sourceFileUrl = uploadFileToS3(
                                findMultipartFileByName(files, srcFile.getLocalFileName()), memberId, projectId, AudioType.VC_SRC
                        );
                        LOGGER.info("소스 파일 S3 업로드 완료: " + sourceFileUrl);

                        String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);
                        LOGGER.info("소스 파일 변환 완료: " + convertedFilePath);

                        saveOutputAudioToS3AndMeta(memberId, convertedFilePath, voiceId, projectId, srcFile);

                        return new VCDetailResDto(
                                null, projectId, srcFile.getIsChecked(), srcFile.getUnitScript(),
                                srcFile.getLocalFileName(), List.of(convertedFilePath)
                        );

                    } catch (IOException e) {
                        LOGGER.severe("소스 파일 처리 실패: " + e.getMessage());
                        throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
                    }
                }).collect(Collectors.toList());
    }

    private void saveOutputAudioToS3AndMeta(Long memberId, String convertedFilePath, String voiceId, Long projectId, AudioFileDto srcFile) {
        // File 객체로 변환
        File convertedFile = new File(convertedFilePath);
        if (!convertedFile.exists()) {
            LOGGER.severe("변환된 파일이 존재하지 않습니다: " + convertedFilePath);
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        // File을 MultipartFile로 변환
        MultipartFile multipartFile = new CustomMultipartFile_team_api(convertedFile);

        // S3에 업로드
        String s3FileUrl = s3Service.uploadUnitSaveFile(multipartFile, memberId, projectId, null);
        LOGGER.info("변환된 파일 S3 업로드 완료: " + s3FileUrl);

        VCProject vcProject = vcProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // VCDetail 생성 및 저장
        VCDetail vcDetail = VCDetail.createVCDetail(vcProject, null);
        vcDetail.updateDetails(srcFile.getIsChecked(), srcFile.getUnitScript());
        vcDetailRepository.save(vcDetail);

        // OutputAudioMeta 저장
        s3Service.saveTTSOrVCOutputAudioMeta(
                generateBucketRoute(s3FileUrl), vcDetail.getId(), ProjectType.VC, s3FileUrl
        );
        LOGGER.info("OutputAudioMeta 저장 완료");
    }

    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream().filter(file -> file.getOriginalFilename().equals(fileName)).findFirst().orElse(null);
    }

    private String uploadFileToS3(MultipartFile file, Long memberId, Long projectId, AudioType audioType) throws IOException {
        return s3Service.uploadAndSaveMemberFile(List.of(file), memberId, projectId, audioType, null).get(0);
    }

    private String generateBucketRoute(String fileUrl) {
        return fileUrl.replaceFirst("https://[^/]+/", "");
    }
}
