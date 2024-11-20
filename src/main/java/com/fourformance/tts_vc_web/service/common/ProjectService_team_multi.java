package com.fourformance.tts_vc_web.service.common;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
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
    private final OutputAudioMetaRepository outputAudioMetaRepository;

    // 프로젝트 삭제 컬럼 업데이트
    @Transactional
    public void deleteProject(Long projectId) {

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
    public void deleteProject(List<Long> ttsDetailIdList) {

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