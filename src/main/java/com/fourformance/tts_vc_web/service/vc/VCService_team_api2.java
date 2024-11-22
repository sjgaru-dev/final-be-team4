package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.repository.*;
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
            System.out.println("뭐가 문제인지 아는 사람? " + projectId);
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
        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files, memberId, memberAudio);

        // src 오디오에 target 오디오 적용
        List<VCDetailResDto> vcDetailsRes = processSourceFiles(vcSrcDetails, files, voiceId, memberId);

        return vcDetailsRes;
    }


    // target 오디오의 목소리 ID 추출
    private String processTargetFiles(List<AudioFileDto> trgFiles, List<MultipartFile> files, Long memberId, MemberAudioMeta memberAudio) {

        if (trgFiles == null || trgFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        // trg 오디오 파일
        MultipartFile file = findMultipartFileByName(files, trgFiles.get(0).getLocalFileName());
        if (file == null) { throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR); }

        try {

            // trg 오디오 가져오기
            String targetFileUrl = memberAudio.getAudioUrl();

            // trg 오디오 Voice Id 생성
            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);

            // MemberAudioMeta 조회
            MemberAudioMeta memberAudioMeta = memberAudioMetaRepository.findById(memberAudio.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_AUDIO));

            // trgVoiceId 업데이트
            memberAudioMeta.update(voiceId);
            memberAudioMetaRepository.resetSelection(memberId, AudioType.VC_TRG);

            return voiceId;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    // src 오디오에 trg 오디오 적용
    private List<VCDetailResDto> processSourceFiles(List<VCDetailDto> srcFiles, List<MultipartFile> files, String voiceId, Long memberId) {

        // src 오디오가 없을 경우, 체크 여부 확인
        if (srcFiles == null || srcFiles.isEmpty()) { throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR); }


        return srcFiles.stream().map(srcFile -> {
            try {
                // src 오디오 파일 가져오기
                String sourceFileUrl = memberAudioMetaRepository.findAudioUrlsByAudioMetaIds(srcFile.getMemberAudioMetaId(), AudioType.VC_SRC);

                // voiceId를 이용해 src 오디오 변환
                String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);

                // S3 오디오 저장
                String vcOutputUrl = s3Service.uploadUnitSaveFile(
                        (MultipartFile) new File(convertedFilePath),
                        memberId,
                        srcFile.getProjectId(),
                        srcFile.getId()
                );


                return new VCDetailResDto(
                        srcFile.getId(), srcFile.getProjectId(), srcFile.getIsChecked(),
                        srcFile.getUnitScript(), sourceFileUrl, List.of(vcOutputUrl)
                );
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
            }
        }).collect(Collectors.toList());
    }

    // 로컬에서 업로드한 파일 정보 file 정보
    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }

}
