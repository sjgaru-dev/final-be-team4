package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
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

    /**
     * VC 프로젝트 처리
     */
    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
        LOGGER.info("VC 프로젝트 처리 시작");

        Member member = findMemberById(memberId);
        Long projectId = saveOrUpdateProject(vcSaveDto, member);
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files, memberId, projectId);

        List<VCDetailResDto> vcDetails = processSourceFiles(vcSaveDto.getSrcFiles(), files, voiceId, projectId, memberId);
        LOGGER.info("VC 프로젝트 처리 완료");

        return vcDetails;
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    LOGGER.severe("멤버 확인 실패: memberId = " + memberId);
                    return new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
                });
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
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        MultipartFile file = findMultipartFileByName(files, trgFiles.get(0).getLocalFileName());
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        try {
            String targetFileUrl = uploadFileToS3(file, memberId, projectId, AudioType.VC_TRG);
            LOGGER.info("타겟 파일 S3 업로드 완료: " + targetFileUrl);

            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);
            LOGGER.info("Voice ID 생성 완료: " + voiceId);

            saveMemberAudioMeta(memberId, targetFileUrl, voiceId, AudioType.VC_TRG);
            return voiceId;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    private List<VCDetailResDto> processSourceFiles(List<AudioFileDto> srcFiles, List<MultipartFile> files, String voiceId, Long projectId, Long memberId) {
        if (srcFiles == null || srcFiles.isEmpty()) {
            return List.of();
        }

        return srcFiles.stream().map(srcFile -> {
            try {
                String sourceFileUrl = uploadOrFindSourceFile(srcFile, files, memberId, projectId);

                String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);
                LOGGER.info("소스 파일 변환 완료: " + convertedFilePath);

                saveOutputAudioMeta(memberId, convertedFilePath, voiceId, projectId, srcFile);

                return new VCDetailResDto(
                        null, projectId, srcFile.getIsChecked(),
                        srcFile.getUnitScript(), srcFile.getLocalFileName(), List.of(convertedFilePath)
                );
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
            }
        }).collect(Collectors.toList());
    }

    private String uploadOrFindSourceFile(AudioFileDto srcFile, List<MultipartFile> files, Long memberId, Long projectId) throws IOException {
        MultipartFile file = findMultipartFileByName(files, srcFile.getLocalFileName());
        if (file != null) {
            return uploadFileToS3(file, memberId, projectId, AudioType.VC_SRC);
        }
        throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
    }

    private void saveOutputAudioMeta(Long memberId, String filePath, String voiceId, Long projectId, AudioFileDto srcFile) {
        VCProject vcProject = vcProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                findMemberById(memberId), filePath, filePath, AudioType.VC_SRC, voiceId
        );
        memberAudioMetaRepository.save(memberAudioMeta);

        VCDetail vcDetail = VCDetail.createVCDetail(vcProject, memberAudioMeta);
        vcDetail.updateDetails(srcFile.getIsChecked(), srcFile.getUnitScript());
        vcDetailRepository.save(vcDetail);
    }

    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    private String uploadFileToS3(MultipartFile file, Long memberId, Long projectId, AudioType audioType) throws IOException {
        return s3Service.uploadAndSaveMemberFile(List.of(file), memberId, projectId, audioType, null).get(0);
    }

    private void saveMemberAudioMeta(Long memberId, String fileUrl, String voiceId, AudioType audioType) {
        Member member = findMemberById(memberId);
        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                member, fileUrl, fileUrl, audioType, voiceId
        );
        memberAudioMetaRepository.save(memberAudioMeta);
    }
}
