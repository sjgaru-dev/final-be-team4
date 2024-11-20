package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VCServiceSave {
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final S3Service s3Service;

    public Long saveVCProject(VCSaveDto vcSaveDto, List<MultipartFile> localFiles) {
        // 1. VCProject 생성/업데이트
        VCProject vcProject = vcSaveDto.getProjectId() == null
                ? createNewVCProject(vcSaveDto)
                : updateExistingVCProject(vcSaveDto);

        // 2. 타겟 파일 처리
        processFiles(vcSaveDto.getTrgFiles(), localFiles, vcProject, AudioType.VC_TRG);

        // 3. 소스 파일 처리
//        processFiles(vcSaveDto.getSrcFiles(), localFiles, vcProject, AudioType.VC_SRC);

        return vcProject.getId();
    }

    private VCProject createNewVCProject(VCSaveDto vcSaveDto) {
        VCProject vcProject = VCProject.createVCProject(null, vcSaveDto.getProjectName());
        vcProjectRepository.save(vcProject);
        return vcProject;
    }

    private VCProject updateExistingVCProject(VCSaveDto vcSaveDto) {
        VCProject vcProject = vcProjectRepository.findById(vcSaveDto.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));
        vcProject.updateVCProject(vcSaveDto.getProjectName(), null);
        return vcProject;
    }

    private void processFiles(List<AudioFileDto> fileDtos, List<MultipartFile> files, VCProject vcProject, AudioType audioType) {
        if (fileDtos == null || fileDtos.isEmpty()) { // 업로드 된 파일이 없을 때
            return;
        }

        for (AudioFileDto fileDto : fileDtos) {
            MemberAudioMeta audioMeta = null;

            if (fileDto.getS3MemberAudioMetaId() != null) {
                // S3 파일 처리
                audioMeta = memberAudioMetaRepository.findById(fileDto.getS3MemberAudioMetaId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_AUDIO));
            } else if (fileDto.getLocalFileName() != null) {
                // 로컬 파일 처리
                MultipartFile localFile = findMultipartFileByName(files, fileDto.getLocalFileName());
                List<String> uploadedUrls = s3Service.uploadAndSaveMemberFile(
                        List.of(localFile), null, vcProject.getId(), audioType, null); // voiceId를 받아오는 api 호출해서 null을 반환값으로 채우면 될 듯
                String fileUrl = uploadedUrls.get(0);

                audioMeta = memberAudioMetaRepository.findFirstByAudioUrl(fileUrl)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_AUDIO));
            }

            if (audioMeta == null) {
                throw new BusinessException(ErrorCode.INVALID_PROJECT_DATA);
            }

            if (audioType == AudioType.VC_TRG) {
                // 타겟 파일은 VCProject에 저장
                vcProject.updateVCProject(vcProject.getProjectName(), audioMeta);
            } else {
                // 소스 파일은 VCDetail에 저장
                VCDetail vcDetail = VCDetail.createVCDetail(vcProject, audioMeta);
                vcDetail.updateDetails(true, fileDto.getUnitScript());
                vcDetailRepository.save(vcDetail);
            }
        }
    }

    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String localFileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(localFileName))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_PROCESSING_ERROR));
    }
}
