//package com.fourformance.tts_vc_web.service.vc;
//
//import com.fourformance.tts_vc_web.common.constant.AudioType;
//import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
//import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
//import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
//import com.fourformance.tts_vc_web.domain.entity.Member;
//import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
//import com.fourformance.tts_vc_web.domain.entity.VCDetail;
//import com.fourformance.tts_vc_web.domain.entity.VCProject;
//import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
//import com.fourformance.tts_vc_web.dto.vc.VCDetailDto;
//import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
//import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
//import com.fourformance.tts_vc_web.repository.*;
//import com.fourformance.tts_vc_web.service.common.S3Service;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class VCService_team_api2 {
//
//    private static final Logger LOGGER = Logger.getLogger(VCService_team_api2.class.getName());
//
//    private final ElevenLabsClient_team_api elevenLabsClient;
//    private final S3Service s3Service;
//    private final MemberRepository memberRepository;
//    private final VCProjectRepository vcProjectRepository;
//    private final VCDetailRepository vcDetailRepository;
//    private final MemberAudioMetaRepository memberAudioMetaRepository;
//    private final OutputAudioMetaRepository outputAudioMetaRepository;
//    private final VCService_team_multi vcService;
//
//    /**
//     * VC 프로젝트 오디오 변환 메서드
//     */
//    public List<VCDetailResDto> processVCProject(VCSaveDto vcSaveDto, List<MultipartFile> files, Long memberId) {
//        LOGGER.info("VC 프로젝트 처리 시작");
//
//        // 멤버 찾기
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//
//        // VC 프로젝트 저장
//        Long projectId = vcService.saveVCProject(vcSaveDto, files, member);
//        VCProject aaaaa = VCProjectRepository.findMemberAudioIdById(projectId);
//
//        // 프로젝트 ID로 VC 상세 반환
//        List<VCDetail> vcDetails = vcDetailRepository.findByVcProject_Id(projectId);
//        List<VCDetailDto> vcSrcDetails = vcDetails.stream()
//                                                  .filter(vcDetail -> vcDetail.getIsChecked() && !vcDetail.getIsDeleted())
//                                                  .map(VCDetailDto::createVCDetailDto) // VCDetail -> VCDetailDto 변환
//                                                  .collect(Collectors.toList());
//
//        // 저장된 trg 오디오 ID 찾기
//        Long memberAudioId = MemberAudioMetaRepository.findById()
//
//        // target 오디오의 목소리 ID 추출
//        String voiceId = processTargetFiles(vcSaveDto.getTrgFiles(), files, memberId, memberAudioId);
//
//        // src 오디오에 target 오디오 적용
//        List<VCDetailResDto> vcDetailsRes = processSourceFiles(vcSrcDetails, files, voiceId, projectId, memberId);
//
//        return vcDetailsRes;
//    }
//
//
//    // target 오디오의 목소리 ID 추출
//    private String processTargetFiles(List<AudioFileDto> trgFiles, List<MultipartFile> files, Long memberId, Long memberAudioId) {
//
//        if (trgFiles == null || trgFiles.isEmpty()) {
//            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
//        }
//
//        // trg 오디오 파일
//        MultipartFile file = findMultipartFileByName(files, trgFiles.get(0).getLocalFileName());
//        if (file == null) { throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR); }
//
//        try {
//
//            // trg 오디오 가져오는 것을 해야함.
//            String targetFileUrl = "";
//
//            // trg 오디오 Voice Id 생성
//            String voiceId = elevenLabsClient.uploadVoice(targetFileUrl);
//
//            // MemberAudioMeta 조회
//            MemberAudioMeta memberAudioMeta = memberAudioMetaRepository.findById(memberAudioId)
//                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_AUDIO));
//
//            // trgVoiceId 업데이트
//            memberAudioMeta.update(voiceId);
//
//            saveMemberAudioMeta(memberId, targetFileUrl, voiceId, AudioType.VC_TRG);
//            return voiceId;
//
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
//        }
//    }
//
//    // src 오디오에 trg 오디오 적용
//    private List<VCDetailResDto> processSourceFiles(List<VCDetailDto> srcFiles, List<MultipartFile> files, String voiceId, Long projectId, Long memberId) {
//
//        // src 오디오가 없을 경우, 체크 여부 확인
//        if (srcFiles == null || srcFiles.isEmpty()) { throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR); }
//
//
//        return srcFiles.stream().map(srcFile -> {
//            try {
//                // src 오디오 파일 가져오기
//                String sourceFileUrl = uploadOrFindSourceFile(srcFile, files, memberId, projectId);
//
//                // voiceId를 이용해 src 오디오 변환
//                String convertedFilePath = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFileUrl);
//
//                // S3 오디오 저장
//                String vcOutputUrl = s3Service.uploadUnitSaveFile(convertedFilePath, memberId, projectId, detailId);
//                //MultipartFile file, Long userId, Long projectId, Long detailId
//
//                vcOutputUrl = "";
//
//
//                return new VCDetailResDto(
//                        null, projectId, srcFile.getIsChecked(),
//                        srcFile.getUnitScript(), srcFile.getLocalFileName(), List.of(convertedFilePath)
//                );
//            } catch (IOException e) {
//                throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
//            }
//        }).collect(Collectors.toList());
//    }
//
//    // src 파일 s3에서 조회 및 업로드
//    private String uploadOrFindSourceFile(AudioFileDto srcFile, List<MultipartFile> files, Long memberId, Long projectId) throws IOException {
//        MultipartFile file = findMultipartFileByName(files, srcFile.getLocalFileName());
//        if (file != null) {
//            return uploadFileToS3(file, memberId, projectId, AudioType.VC_SRC);
//        }
//        throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
//    }
//
//
//    // 로컬에서 업로드한 파일 정보 file 정보
//    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String fileName) {
//        return files.stream()
//                .filter(file -> file.getOriginalFilename().equals(fileName))
//                .findFirst()
//                .orElse(null);
//    }
//
//    private String uploadFileToS3(MultipartFile file, Long memberId, Long projectId, AudioType audioType) throws IOException {
//        return s3Service.uploadAndSaveMemberFile(List.of(file), memberId, projectId, audioType).get(0);
//    }
//
//    // MemberAudioMeta에 저장
//    private void saveMemberAudioMeta(Long memberId, String fileUrl, String voiceId, AudioType audioType) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//        MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(
//                member, fileUrl, fileUrl, audioType
//        );
//        memberAudioMetaRepository.save(memberAudioMeta);
//    }
//}
