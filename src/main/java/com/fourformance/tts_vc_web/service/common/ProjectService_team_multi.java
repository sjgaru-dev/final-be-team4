package com.fourformance.tts_vc_web.service.common;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService_team_multi {

    private final ProjectRepository projectRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final VCDetailRepository vcDetailRepository;
    private final OutputAudioMetaRepository outputAudioMetaRepository;
    private final MemberAudioVCRepository memberAudioVCRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;

    // VC 프로젝트 삭제 컬럼 업데이트
    @Transactional
    public void deleteVCProject(Long projectId) {

        // 1. 프로젝트 조회 및 isDeleted 설정
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        project.deletedAt(); // deletedAt 설정
        projectRepository.save(project);

        try {
            // 2. VCDetail 조회 및 isDeleted 설정
            List<VCDetail> vcDetails = vcDetailRepository.findByVcProjectId(projectId);
            for (VCDetail detail : vcDetails) {
                detail.markAsDeleted();
                vcDetailRepository.save(detail);
            }

            // 3. VC 생성된 오디오 조회 및 isDeleted 설정
            // TTSDetail ID 리스트 추출
            List<Long> vcDetailIds = vcDetails.stream()
                    .map(VCDetail::getId)
                    .toList();

            // OutputAudioMeta 삭제 처리
            List<OutputAudioMeta> outputAudio = outputAudioMetaRepository.findByTtsDetailAndIsDeletedFalse(vcDetailIds);
            for (OutputAudioMeta audio : outputAudio) {
                audio.deleteOutputAudioMeta();
                outputAudioMetaRepository.save(audio);
            }

            // 4. VC src 오디오 삭제

            List<Long> memberAudioIds = memberAudioVCRepository.findMemberAudioMetaByVcProjectId(projectId);
            List<MemberAudioMeta> memberAudioList = memberAudioMetaRepository.findByMemberAudioIds(memberAudioIds, AudioType.VC_SRC);

            for (MemberAudioMeta memberAudioMeta : memberAudioList) {
                memberAudioMeta.delete(); // isDeleted = true, deletedAt 설정
                memberAudioMetaRepository.save(memberAudioMeta); // 업데이트 저장
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }

    }


    // TTS 프로젝트 삭제 컬럼 업데이트
    @Transactional
    public void deleteTTSProject(Long projectId) {

        // 1. 프로젝트 조회 및 isDeleted 설정
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        project.deletedAt(); // deletedAt 설정
        projectRepository.save(project);

        try {
            // 2. TTSDetail 조회 및 isDeleted 설정
            List<TTSDetail> ttsDetails = ttsDetailRepository.findByTtsProjectId(projectId);
            for (TTSDetail detail : ttsDetails) {
                detail.deleteTTSDetail();
                ttsDetailRepository.save(detail);
            }

            // 3. TTS 생성된 오디오 조회 및 isDeleted 설정
            // TTSDetail ID 리스트 추출
            List<Long> ttsDetailIds = ttsDetails.stream()
                    .map(TTSDetail::getId)
                    .toList();

            // OutputAudioMeta 삭제 처리
            List<OutputAudioMeta> outputAudio = outputAudioMetaRepository.findByTtsDetailAndIsDeletedFalse(ttsDetailIds);
            for (OutputAudioMeta audio : outputAudio) {
                audio.deleteOutputAudioMeta();
                outputAudioMetaRepository.save(audio);
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }

    }

    // TTS 선택된 모든 항목 삭제
    @Transactional
    public void deleteTTSDetail(List<Long> ttsDetailIdList) {

        // TTSDetail ID 리스트를 사용하여 TTSDetail 엔티티를 조회
        List<TTSDetail> ttsDetails = ttsDetailRepository.findByTtsDetailIds(ttsDetailIdList);

        // TTSDetail ID 리스트 추출
        List<Long> ttsDetailIds = ttsDetails.stream()
                .map(TTSDetail::getId)
                .toList();

        if(ttsDetailIdList.size() != ttsDetailIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_ID);
        }

        try {

            // 1. TTSDetail 삭제 처리
            for (TTSDetail detail : ttsDetails) {
                detail.deleteTTSDetail();
                ttsDetailRepository.save(detail);
            }

            // 2. OutputAudioMeta 삭제 처리
            List<OutputAudioMeta> outputAudio = outputAudioMetaRepository.findByTtsDetailAndIsDeletedFalse(ttsDetailIds);
            for (OutputAudioMeta audio : outputAudio) {
                audio.deleteOutputAudioMeta();
                outputAudioMetaRepository.save(audio);
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }

}