package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.domain.entity.Style;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.dto.tts.TtsDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TtsProjectDetailDto;
import com.fourformance.tts_vc_web.repository.StyleRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TTSService_team_multi {

    private final TTSProjectRepository ttsProjectRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final StyleRepository styleRepository;

    //해당 프로젝트가 없으면 생성하고, 이미 있으면 update쳐야함 => 관심사 분리 해야할 것 같음
    //unitSequence도 순서대로 잘 들어왔는지, 중복된 값은 없는지 체크 필요
    //projectId는 존재하고 detailId를 모두 null로 한 테스트 통과함
    public Long saveTTSProjectAndDetail(TtsProjectDetailDto dto) {
        TTSProject ttsProject;

        // projectId가 null이면 새 프로젝트 생성
        if (dto.getProjectId() == null) {
            ttsProject = TTSProject.createTTSProject(null, dto.getProjectName(), dto.getFullScript(), dto.getGlobalSpeed(),dto.getGlobalPitch(),dto.getGlobalVolume());
        } else {
            // projectId가 있으면 기존 프로젝트 조회 및 업데이트
            ttsProject = ttsProjectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Project with ID " + dto.getProjectId() + " not found"));
            ttsProject.updateTTSProject(dto.getProjectName(), dto.getFullScript(), dto.getGlobalSpeed(), dto.getGlobalPitch(), dto.getGlobalVolume());
        }

        // 프로젝트 저장
        ttsProject = ttsProjectRepository.save(ttsProject);

        // TTSDetail 리스트가 null인지 확인
        if (dto.getTtsDetails() != null) {
            // TTSDetail 리스트를 처리
            for (TtsDetailDto detailDto : dto.getTtsDetails()) {
                Style detailStyle = styleRepository.findById(detailDto.getDetailStyleId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid detail style ID"));

                TTSDetail ttsDetail;
                if (detailDto.getDetailId() != null) {
                    // detailId가 있으면 기존 TTSDetail 조회 및 업데이트
                    ttsDetail = ttsDetailRepository.findById(detailDto.getDetailId())
                            .orElseThrow(() -> new IllegalArgumentException("Detail with ID " + detailDto.getDetailId() + " not found"));
                    ttsDetail.updateTTSDetail(detailStyle, detailDto.getUnitScript(), detailDto.getUnitSpeed(), detailDto.getUnitPitch(), detailDto.getUnitVolume(), detailDto.getUnitSequence(), detailDto.getIsDeleted());
                } else {
                    // detailId가 없으면 새 TTSDetail 생성
                    ttsDetail = TTSDetail.createTTSDetail(ttsProject, detailDto.getUnitScript(), detailDto.getUnitSequence());
                    ttsDetail.updateTTSDetail(detailStyle, detailDto.getUnitScript(), detailDto.getUnitSpeed(), detailDto.getUnitPitch(), detailDto.getUnitVolume(), detailDto.getUnitSequence(), detailDto.getIsDeleted());
                }
                // ttsDetail 객체 저장
                ttsDetailRepository.save(ttsDetail);
            }
        }

        return ttsProject.getId();  // 저장된 TTSProject의 ID 반환
    }

}
