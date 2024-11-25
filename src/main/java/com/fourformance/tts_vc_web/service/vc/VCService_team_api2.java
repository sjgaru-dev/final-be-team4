package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
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


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
    private final VCService_team_multi vcService;

    /**
     * VC 프로젝트 오디오 변환 메서드
     */
    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
        LOGGER.info("VC 프로젝트 처리 시작");

        // 멤버 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // VC 프로젝트 저장
        Long projectId = vcService.saveVCProject(vcSaveDto, files, member);
        if(projectId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND); }

        // 프로젝트 ID로 VC 상세 반환
        List<VCDetail> vcDetails = vcDetailRepository.findByVcProject_Id(projectId);
        List<VCDetailDto> vcSrcDetails = vcDetails.stream()
                                                  .filter(vcDetail -> vcDetail.getIsChecked() && !vcDetail.getIsDeleted())
                                                  .map(VCDetailDto::createVCDetailDto) // VCDetail -> VCDetailDto 변환
                                                  .collect(Collectors.toList());

        // 저장된 trg 오디오 ID 찾기
        MemberAudioMeta memberAudio = memberAudioMetaRepository.findSelectedAudioByTypeAndMember(AudioType.VC_TRG, memberId);

        // target 오디오의 목소리 ID 추출
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), memberAudio);

        // src 오디오에 target 오디오 적용
        List<VCDetailResDto> vcDetailsRes = processSourceFiles(files, vcSrcDetails, voiceId, memberId);

        return vcDetailsRes;
    }


    // target 오디오의 목소리 ID 추출
    private String processTargetFiles(List<TrgAudioFileDto> trgFiles, MemberAudioMeta memberAudio) {

        if (trgFiles == null || trgFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        try {

            // trg 오디오 가져오기
            String targetFileUrl = memberAudio.getAudioUrl(); //nullpointException 지점

            // trg 오디오 Voice Id 생성
            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);

            // MemberAudioMeta 조회
            MemberAudioMeta memberAudioMeta = memberAudioMetaRepository.findById(memberAudio.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_AUDIO));

            // trgVoiceId 업데이트
            memberAudioMeta.update(voiceId);
            memberAudioMetaRepository.save(memberAudioMeta);

            return voiceId;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    // src 오디오에 trg 오디오 적용
    private List<VCDetailResDto> processSourceFiles( List<MultipartFile> inputFiles, List<VCDetailDto> srcFiles, String voiceId, Long memberId) {

        // srcFiles와 inputFiles를 매칭하여 처리
        return srcFiles.stream()
                .map(srcFile -> {
                    MultipartFile matchingFile = findMultipartFileByName(inputFiles, srcFile.getLocalFile().getOriginalFilename()); // (수정 필요) src 파일 이름 매칭
                    if (matchingFile != null) {
                        return processSingleSourceFile(srcFile, matchingFile, voiceId, memberId);
                    }
                    return null; // 매칭되지 않는 경우 null 반환
                })
                .filter(Objects::nonNull) // null 제거
                .collect(Collectors.toList());
    }

    // 단일 src 파일 처리 메서드
    private VCDetailResDto processSingleSourceFile(VCDetailDto srcFile, MultipartFile originFile, String voiceId, Long memberId) {
        try {
            // src 오디오 파일 가져오기
            String sourceFileUrl = memberAudioMetaRepository.findAudioUrlsByAudioMetaIds(
                    srcFile.getMemberAudioMetaId(),
                    AudioType.VC_SRC
            );

            // voiceId를 이용해 src 오디오 변환
            String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);

            // 변환된 파일을 S3에 저장
            String vcOutputUrl = s3Service.uploadUnitSaveFile(
                    originFile,
                    memberId,
                    srcFile.getProjectId(),
                    srcFile.getId()
            );

            // 결과 DTO 생성 및 반환
            return new VCDetailResDto(
                    srcFile.getId(),
                    srcFile.getProjectId(),
                    srcFile.getIsChecked(),
                    srcFile.getUnitScript(),
                    sourceFileUrl,
                    List.of(vcOutputUrl)
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }


    // 로컬에서 업로드한 파일 정보 file 정보
    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }

}
