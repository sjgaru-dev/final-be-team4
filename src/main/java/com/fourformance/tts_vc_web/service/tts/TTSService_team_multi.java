package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectWithDetailsDto;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.repository.VoiceStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TTSService_team_multi {

    private final TTSProjectRepository ttsProjectRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final VoiceStyleRepository voiceStyleRepository;

    //해당 프로젝트가 없으면 생성하고, 이미 있으면 update쳐야함 => 관심사 분리 해야할 것 같음
    //unitSequence도 순서대로 잘 들어왔는지, 중복된 값은 없는지 체크 필요
    //projectId는 존재하고 detailId를 모두 null로 한 테스트 통과함
    public Long saveTTSProjectAndDetail(TTSProjectWithDetailsDto dto) {
        TTSProject ttsProject;
        TTSProjectDto prjDto = (TTSProjectDto) dto.getTtsProject();
        List<TTSDetailDto> detailDtoList = (List<TTSDetailDto>) dto.getTtsDetails();


        //dto에서는 voiceStyleId를 Long타입으로 받고 있지만, ttsProject 생성 메서드에서는 VoiceStyle객체를 매개변수로 넘겨야함
        VoiceStyle voiceStyle = voiceStyleRepository.findById(prjDto.getVoiceStyleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid VoiceStyle ID: " + prjDto.getVoiceStyleId()));

        // projectId가 null이면 새 프로젝트 생성
        if (prjDto.getId() == null) {
            ttsProject = TTSProject.createTTSProject(null, prjDto.getProjectName(), voiceStyle, prjDto.getFullScript(), prjDto.getGlobalSpeed(),prjDto.getGlobalPitch(),prjDto.getGlobalVolume());
        } else {
            // projectId가 있으면 기존 프로젝트 조회 및 업데이트
            ttsProject = ttsProjectRepository.findById(prjDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project with ID " + prjDto.getId() + " not found"));
            ttsProject.updateTTSProject(prjDto.getProjectName(), voiceStyle,prjDto.getFullScript(), prjDto.getGlobalSpeed(),prjDto.getGlobalPitch(),prjDto.getGlobalVolume());
        }

        // 프로젝트 저장
        ttsProject = ttsProjectRepository.save(ttsProject);

        // TTSDetail 리스트가 null인지 확인
        if (detailDtoList != null) {
            // TTSDetail 리스트를 처리
            for (TTSDetailDto detailDto : detailDtoList) {
                VoiceStyle detailStyle = voiceStyleRepository.findById(detailDto.getVoiceStyleId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid detail VoiceStyle ID"));

                TTSDetail ttsDetail;
                if (detailDto.getId() != null) {
                    // detailId가 있으면 기존 TTSDetail 조회 및 업데이트
                    ttsDetail = ttsDetailRepository.findById(detailDto.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Detail with ID " + detailDto.getId() + " not found"));
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

    // TTS 프로젝트 값 조회하기
    @Transactional(readOnly = true)
    public TTSProjectDto getTTSProjectDto(Long projectId) {
        // 프로젝트 조회
        TTSProject ttsProject = ttsProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // TTSProjectDTO로 변환
        return TTSProjectDto.createTTSProjectDto(ttsProject);
    }

    // TTS 프로젝트 상세 값 조회하기
    @Transactional(readOnly = true)
    public List<TTSDetailDto> getTTSDetailsDto(Long projectId) {
        List<TTSDetail> ttsDetails = ttsDetailRepository.findByTtsProjectId(projectId);

        // isDeleted가 false인 경우에만 TTSDetailDTO 목록으로 변환
        return ttsDetails.stream()
                .filter(detail -> !detail.getIsDeleted()) // isDeleted가 false인 경우만 필터링
                .map(TTSDetailDto::createTTSDetailDto) // ModelMapper를 통해 TTSDetailDto로 변환
                .collect(Collectors.toList());
    }

}
