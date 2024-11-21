package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCService_team_api.class.getName());
    private final ElevenLabsClient_team_api elevenLabsClient;
    private final MemberRepository memberRepository;
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final S3Service s3Service; // S3Service 주입

    @Value("${user.home}/uploads")
    private String uploadDir;

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

        // 2. 프로젝트 생성/업데이트
        Long projectId = saveOrUpdateProject(vcSaveDto, member);
        LOGGER.info("프로젝트 생성/업데이트 완료: projectId = " + projectId);

        // 3. 타겟 파일 처리
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files);
        LOGGER.info("타겟 파일 처리 완료: voiceId = " + voiceId);

        // 4. 소스 파일 처리 및 VCDetailResDto 생성
        List<VCDetailResDto> vcDetails = processSourceFiles(vcSaveDto.getSrcFiles(), files, projectId, voiceId);
        LOGGER.info("소스 파일 처리 완료, VCDetailResDto 리스트 생성");

        LOGGER.info("VC 프로젝트 처리 완료");
        return vcDetails;
    }

    private Long saveOrUpdateProject(VCSaveDto vcSaveDto, Member member) {
        if (vcSaveDto.getProjectId() == null) {
            LOGGER.info("새 프로젝트 생성 중...");
            VCProject vcProject = VCProject.createVCProject(member, vcSaveDto.getProjectName());
            vcProjectRepository.save(vcProject);
            LOGGER.info("새 프로젝트 생성 완료: projectId = " + vcProject.getId());
            return vcProject.getId();
        } else {
            LOGGER.info("기존 프로젝트 업데이트 중...");
            VCProject vcProject = vcProjectRepository.findById(vcSaveDto.getProjectId())
                    .orElseThrow(() -> {
                        LOGGER.severe("프로젝트 업데이트 실패: projectId = " + vcSaveDto.getProjectId());
                        return new BusinessException(ErrorCode.NOT_EXISTS_PROJECT);
                    });
            vcProject.updateVCProject(vcSaveDto.getProjectName(), null);
            LOGGER.info("기존 프로젝트 업데이트 완료: projectId = " + vcSaveDto.getProjectId());
            return vcProject.getId();
        }
    }

    private String processTargetFiles(List<AudioFileDto> trgFiles, List<MultipartFile> files) {
        if (trgFiles == null || trgFiles.isEmpty()) {
            LOGGER.warning("타겟 파일이 없습니다.");
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
        LOGGER.info("타겟 파일 처리 시작");

        AudioFileDto targetFile = trgFiles.get(0); // 타겟 파일은 하나만 처리
        MultipartFile file = findMultipartFileByName(files, targetFile.getLocalFileName());
        if (file == null) {
            LOGGER.severe("타겟 파일 로드 실패: " + targetFile.getLocalFileName());
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        try {
            String localFilePath = saveFileLocally(file);
            LOGGER.info("타겟 파일 로컬 저장 완료: " + localFilePath);
            String voiceId = elevenLabsClient.uploadVoice(localFilePath);
            LOGGER.info("Voice ID 생성 완료: " + voiceId);
            return voiceId;
        } catch (IOException e) {
            LOGGER.severe("타겟 파일 처리 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    private List<VCDetailResDto> processSourceFiles(List<AudioFileDto> srcFiles, List<MultipartFile> files, Long projectId, String voiceId) {
        if (srcFiles == null || srcFiles.isEmpty()) {
            LOGGER.warning("소스 파일이 없습니다.");
            return List.of();
        }
        LOGGER.info("소스 파일 처리 시작");

        return srcFiles.stream()
                .map(srcFile -> {
                    MultipartFile file = findMultipartFileByName(files, srcFile.getLocalFileName());
                    if (file == null) {
                        LOGGER.warning("소스 파일 로드 실패: " + srcFile.getLocalFileName());
                        return null;
                    }

                    try {
                        String localFilePath = saveFileLocally(file);
                        LOGGER.info("소스 파일 로컬 저장 완료: " + localFilePath);
                        String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, localFilePath);
                        LOGGER.info("소스 파일 변환 완료: " + convertedFilePath);

                        // VCDetail 엔티티 저장
                        VCDetail vcDetail = VCDetail.createVCDetail(
                                vcProjectRepository.findById(projectId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT)),
                                null // 필요 시 MemberAudioMeta 설정
                        );
                        vcDetail.updateDetails(srcFile.getIsChecked(), srcFile.getUnitScript());
                        vcDetailRepository.save(vcDetail);

                        // VCDetailResDto 반환
                        return new VCDetailResDto(
                                vcDetail.getId(),
                                projectId,
                                srcFile.getIsChecked(),
                                srcFile.getUnitScript(),
                                file.getOriginalFilename(),
                                List.of(convertedFilePath)
                        );
                    } catch (IOException e) {
                        LOGGER.severe("소스 파일 처리 실패: " + e.getMessage());
                        throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
                    }
                })
                .collect(Collectors.toList());
    }

    private String saveFileLocally(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String localFilePath = uploadDir + File.separator + file.getOriginalFilename();
        file.transferTo(new File(localFilePath));
        LOGGER.info("로컬 파일 저장 완료: " + localFilePath);
        return localFilePath;
    }

    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }
}
