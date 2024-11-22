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

    private final ElevenLabsClient_team_api elevenLabsClient; // Eleven Labs API 호출 도구
    private final S3Service s3Service; // S3 파일 관리 도구
    private final MemberRepository memberRepository; // 멤버 정보 저장소
    private final VCProjectRepository vcProjectRepository; // 프로젝트 정보 저장소
    private final VCDetailRepository vcDetailRepository; // 디테일 정보 저장소
    private final MemberAudioMetaRepository memberAudioMetaRepository; // 오디오 메타정보 저장소
    private final OutputAudioMetaRepository outputAudioMetaRepository; // 출력 오디오 메타정보 저장소

    /**
     * VC 프로젝트 처리
     */
    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
        LOGGER.info("VC 프로젝트 처리 시작");

        // 1. 멤버 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    LOGGER.severe("멤버 확인 실패: memberId = " + memberId);
                    return new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
                });
        LOGGER.info("멤버 확인 완료: memberId = " + memberId);

        // 2. 프로젝트 저장 또는 업데이트
        Long projectId = saveOrUpdateProject(vcSaveDto, member);
        LOGGER.info("프로젝트 저장/업데이트 완료: projectId = " + projectId);

        // 3. 타겟 파일 처리 및 Voice ID 생성
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files, memberId, projectId);
        LOGGER.info("타겟 파일 처리 완료: Voice ID = " + voiceId);

        // 4. 소스 파일 변환
        List<VCDetailResDto> vcDetails = processSourceFiles(vcSaveDto.getSrcFiles(), files, voiceId, projectId, memberId);
        LOGGER.info("소스 파일 처리 완료");

        LOGGER.info("VC 프로젝트 처리 완료");
        return vcDetails;
    }

    /**
     * 프로젝트 저장 또는 업데이트
     */
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

    /**
     * 타겟 파일 처리 및 Voice ID 생성
     */
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
            // 1. S3 업로드
            String targetFileUrl = uploadFileToS3(file, memberId, projectId, AudioType.VC_TRG);
            LOGGER.info("타겟 파일 S3 업로드 완료: " + targetFileUrl);

            // 2. Eleven Labs Voice ID 생성
            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);
            LOGGER.info("Voice ID 생성 완료: " + voiceId);

            // 3. MemberAudioMeta 생성 및 저장
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                    member,
                    targetFileUrl,
                    targetFileUrl,
                    AudioType.VC_TRG,
                    voiceId
            );
            memberAudioMetaRepository.save(memberAudioMeta); // 저장
            LOGGER.info("MemberAudioMeta 저장 완료: ID = " + memberAudioMeta.getId());

            // 4. VCProject 업데이트
            VCProject vcProject = vcProjectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

            vcProject.updateVCProject(vcProject.getProjectName(), memberAudioMeta); // MemberAudioMeta 설정
            vcProject.updateTrgVoiceId(voiceId); // Voice ID 설정
            vcProjectRepository.save(vcProject); // 변경 사항 저장
            LOGGER.info("VCProject 업데이트 완료: MemberAudioMeta ID = " + memberAudioMeta.getId());

            return voiceId;

        } catch (IOException e) {
            LOGGER.severe("타겟 파일 처리 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }





    /**
     * 소스 파일 처리 및 변환
     */
    private List<VCDetailResDto> processSourceFiles(List<AudioFileDto> srcFiles, List<MultipartFile> files, String voiceId, Long projectId, Long memberId) {
        if (srcFiles == null || srcFiles.isEmpty()) {
            LOGGER.warning("소스 파일이 없습니다.");
            return List.of();
        }

        return srcFiles.stream()
                .map(srcFile -> {
                    try {
                        String sourceFileUrl;

                        // S3 업로드 또는 기존 S3 URL 사용
                        MultipartFile file = findMultipartFileByName(files, srcFile.getLocalFileName());
                        if (file != null) {
                            sourceFileUrl = uploadFileToS3(file, memberId, projectId, AudioType.VC_SRC);
                            LOGGER.info("소스 파일 S3 업로드 완료: " + sourceFileUrl);
                        } else {
                            LOGGER.warning("소스 파일 로드 실패: " + srcFile.getLocalFileName());
                            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
                        }

                        // Eleven Labs API 호출
                        String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);
                        LOGGER.info("소스 파일 변환 완료: " + convertedFilePath);

                        // VCDetail 및 MemberAudioMeta 생성
                        Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
                        VCProject vcProject = vcProjectRepository.findById(projectId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

                        // MemberAudioMeta 생성 및 저장
                        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                                member,
                                convertedFilePath,
                                convertedFilePath,
                                AudioType.VC_SRC,
                                voiceId
                        );
                        memberAudioMetaRepository.save(memberAudioMeta);
                        LOGGER.info("MemberAudioMeta 저장 완료: ID = " + memberAudioMeta.getId());

                        // VCDetail 생성 및 저장
                        VCDetail vcDetail = VCDetail.createVCDetail(vcProject, memberAudioMeta);
                        vcDetail.updateDetails(srcFile.getIsChecked(), srcFile.getUnitScript());
                        vcDetailRepository.save(vcDetail);
                        LOGGER.info("VCDetail 저장 완료: ID = " + vcDetail.getId());

                        // VCDetailResDto 반환
                        return new VCDetailResDto(
                                vcDetail.getId(),
                                projectId,
                                srcFile.getIsChecked(),
                                srcFile.getUnitScript(),
                                srcFile.getLocalFileName(),
                                List.of(convertedFilePath)
                        );
                    } catch (IOException e) {
                        LOGGER.severe("소스 파일 처리 실패: " + e.getMessage());
                        throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
                    }
                })
                .collect(Collectors.toList());
    }



    /**
     * OutputAudioMeta 저장
     */
    private void saveOutputAudioMeta(String outputAudioPath, Long projectId) {
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(
                outputAudioPath,
                null, null, null,
                ProjectType.VC,
                "Generated audio"
        );
        outputAudioMetaRepository.save(outputAudioMeta);
        LOGGER.info("OutputAudioMeta 저장 완료: " + outputAudioPath);
    }

    /**
     * 파일 이름으로 MultipartFile 찾기
     */
    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    /**
     * S3에 파일 업로드
     */
    private String uploadFileToS3(MultipartFile file, Long memberId, Long projectId, AudioType audioType) throws IOException {
        List<String> uploadedUrls = s3Service.uploadAndSaveMemberFile(List.of(file), memberId, projectId, audioType, null);
        return uploadedUrls.get(0);
    }

    private void saveMemberAudioMeta(Long memberId, String fileUrl, String voiceId, AudioType audioType) {
        // 1. 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. MemberAudioMeta 생성
        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                member,
                generateBucketRoute(fileUrl),
                fileUrl,
                audioType,
                voiceId
        );

        // 3. 저장
        memberAudioMetaRepository.save(memberAudioMeta);
        LOGGER.info("MemberAudioMeta 저장 완료: " + memberAudioMeta.getId());
    }

    private String generateBucketRoute(String fileUrl) {
        return fileUrl.replaceFirst("https://[^/]+/", ""); // URL에서 도메인을 제거하여 경로 반환
    }

}
