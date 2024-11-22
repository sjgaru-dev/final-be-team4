package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.repository.*;
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
public class VCService_team_api2 {

    private static final Logger LOGGER = Logger.getLogger(VCService_team_api2.class.getName());

    private final ElevenLabsClient_team_api elevenLabsClient;
    private final S3Service s3Service;
    private final MemberRepository memberRepository;
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final OutputAudioMetaRepository outputAudioMetaRepository;

    /**
     * VC 프로젝트 오디오 변환 메서드
     */
    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
        LOGGER.info("VC 프로젝트 처리 시작");

        // 멤버 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 프로젝트 저장
        Long projectId = saveOrUpdateProject(vcSaveDto, member);

        // target 오디오의 목소리 ID 추출
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files, memberId, projectId);

        // src 오디오에 target 오디오 적용
        List<VCDetailResDto> vcDetails = processSourceFiles(vcSaveDto.getSrcFiles(), files, voiceId, projectId, memberId);

        LOGGER.info("VC 프로젝트 처리 완료");

        return vcDetails;
    }

    // VC 프로젝트 저장
    private Long saveOrUpdateProject(VCSaveDto vcSaveDto, Member member) {

        Long projectId = vcSaveDto.getProjectId();

        if (projectId == null) { // 저장된 VC 프로젝트가 없을 경우
            VCProject vcProject = VCProject.createVCProject(member, vcSaveDto.getProjectName());
            vcProjectRepository.save(vcProject);

            LOGGER.info("새 프로젝트 생성 완료: projectId = " + vcProject.getId());
            projectId = vcProject.getId();

        } else { // 저장된 VC 프로젝트가 있는 경우
            VCProject vcProject = vcProjectRepository.findById(projectId)
                    .orElseThrow(() -> { return new BusinessException(ErrorCode.NOT_EXISTS_PROJECT); });

            //

            vcProject.updateVCProject(vcSaveDto.getProjectName(), null);                // target오디오가 null?!
        }

        return projectId;
    }

    private String processTargetFiles(List<AudioFileDto> trgFiles, List<MultipartFile> files, Long memberId, Long projectId) {

        if (trgFiles == null || trgFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        // trg 오디오 파일
        MultipartFile file = findMultipartFileByName(files, trgFiles.get(0).getLocalFileName());
        if (file == null) { throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR); }

        try {

            // trg 오디오 s3에 업로드
            String targetFileUrl = uploadFileToS3(file, memberId, projectId, AudioType.VC_TRG);

            // trg 오디오 Voice Id 생성
            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);

            // MemberAudioMeta에 저장
            saveMemberAudioMeta(memberId, targetFileUrl, voiceId, AudioType.VC_TRG);
            return voiceId;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    // src 오디오에 trg 오디오 적용
    private List<VCDetailResDto> processSourceFiles(List<AudioFileDto> srcFiles, List<MultipartFile> files, String voiceId, Long projectId, Long memberId) {

        // src 오디오가 없을 경우
        if (srcFiles == null || srcFiles.isEmpty() || voiceId == null) {
            return List.of();
        }

        return srcFiles.stream().map(srcFile -> {
            try {
                // src 오디오 파일 가져오기
                String sourceFileUrl = uploadOrFindSourceFile(srcFile, files, memberId, projectId);

                // voiceId를 이용해 src 오디오 변환
                String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);

                // S3 오디오 저장


                // output 오디오 저장
//                saveOutputAudioMeta(memberId, convertedFilePath, voiceId, projectId, srcFile);

                return new VCDetailResDto(
                        null, projectId, srcFile.getIsChecked(),
                        srcFile.getUnitScript(), srcFile.getLocalFileName(), List.of(convertedFilePath)
                );
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
            }
        }).collect(Collectors.toList());
    }

    // src 파일 s3에서 조회 및 업로드
    private String uploadOrFindSourceFile(AudioFileDto srcFile, List<MultipartFile> files, Long memberId, Long projectId) throws IOException {
        MultipartFile file = findMultipartFileByName(files, srcFile.getLocalFileName());
        if (file != null) {
            return uploadFileToS3(file, memberId, projectId, AudioType.VC_SRC);
        }
        throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
    }

    // VC 생성된 오디오 저장
//    private void saveOutputAudioMeta(Long memberId, String filePath, String voiceId, Long projectId, AudioFileDto srcFile) {
//        VCProject vcProject = vcProjectRepository.findById(projectId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));
//
//        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
//                findMemberById(memberId), filePath, filePath, AudioType.VC_SRC, voiceId
//        );
//        memberAudioMetaRepository.save(memberAudioMeta);
//
//        VCDetail vcDetail = VCDetail.createVCDetail(vcProject, memberAudioMeta);
//        vcDetail.updateDetails(srcFile.getIsChecked(), srcFile.getUnitScript());
//        vcDetailRepository.save(vcDetail);
//    }

    // 로컬에서 업로드한 파일 정보 file 정보
    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    private String uploadFileToS3(MultipartFile file, Long memberId, Long projectId, AudioType audioType) throws IOException {
        return s3Service.uploadAndSaveMemberFile(List.of(file), memberId, projectId, audioType, null).get(0);
    }

    // MemberAudioMeta에 저장
    private void saveMemberAudioMeta(Long memberId, String fileUrl, String voiceId, AudioType audioType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
                member, fileUrl, fileUrl, audioType, voiceId
        );
        memberAudioMetaRepository.save(memberAudioMeta);
    }
}
