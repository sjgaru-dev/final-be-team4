package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectWithDetailsDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.repository.VoiceStyleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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


    //unitSequence도 순서대로 잘 들어왔는지, 중복된 값은 없는지 체크 필요
    //projectId는 존재하고 detailId를 모두 null로 테스트 시도

    // 프로젝트 생성
    @Transactional
    public Long createNewProject(TTSSaveDto dto) {
        VoiceStyle voiceStyle = voiceStyleRepository.findById(dto.getVoiceStyleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        // TTSProject 생성
        TTSProject ttsProject = TTSProject.createTTSProject(
                null,
                dto.getProjectName(),
                voiceStyle,
                dto.getFullScript(),
                dto.getGlobalSpeed(),
                dto.getGlobalPitch(),
                dto.getGlobalVolume()
        );
        ttsProject = ttsProjectRepository.save(ttsProject);

        if (dto.getTtsDetails() != null) {
            for (TTSDetailDto detailDto : dto.getTtsDetails()) {
                createTTSDetail(detailDto, ttsProject);
            }
        }
        return ttsProject.getId();
    }

    // 프로젝트 업데이트
    @Transactional
    public Long updateProject(TTSSaveDto dto) {
        TTSProject ttsProject = ttsProjectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        VoiceStyle voiceStyle = voiceStyleRepository.findById(dto.getVoiceStyleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT)); //

        ttsProject.updateTTSProject(
                dto.getProjectName(),
                voiceStyle,
                dto.getFullScript(),
                dto.getGlobalSpeed(),
                dto.getGlobalPitch(),
                dto.getGlobalVolume()
        );

        if (dto.getTtsDetails() != null) {
            for (TTSDetailDto detailDto : dto.getTtsDetails()) {
                processTTSDetail(detailDto, ttsProject);
            }
        }
        return ttsProject.getId();
    }

    // ttsDetail 생성 메서드
    private void createTTSDetail(TTSDetailDto detailDto, TTSProject ttsProject) {
        VoiceStyle detailStyle = voiceStyleRepository.findById(detailDto.getVoiceStyleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        TTSDetail ttsDetail = TTSDetail.createTTSDetail(
                ttsProject,
                detailDto.getUnitScript(),
                detailDto.getUnitSequence()
        );
        ttsDetail.updateTTSDetail(
                detailStyle,
                detailDto.getUnitScript(),
                detailDto.getUnitSpeed(),
                detailDto.getUnitPitch(),
                detailDto.getUnitVolume(),
                detailDto.getUnitSequence(),
                detailDto.getIsDeleted()
        );

        ttsDetailRepository.save(ttsDetail);
    }

    // ttsDetail 업데이트 메서드
    private void processTTSDetail(TTSDetailDto detailDto, TTSProject ttsProject) {
        VoiceStyle detailStyle = voiceStyleRepository.findById(detailDto.getVoiceStyleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        if (detailDto.getId() != null) {
            // 기존 TTSDetail 업데이트
            TTSDetail ttsDetail = ttsDetailRepository.findById(detailDto.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));
            ttsDetail.updateTTSDetail(
                    detailStyle,
                    detailDto.getUnitScript(),
                    detailDto.getUnitSpeed(),
                    detailDto.getUnitPitch(),
                    detailDto.getUnitVolume(),
                    detailDto.getUnitSequence(),
                    detailDto.getIsDeleted()
            );
            ttsDetailRepository.save(ttsDetail);
        } else {
            // 새로운 TTSDetail 생성 메서드 호출
            createTTSDetail(detailDto, ttsProject);
        }
    }


}
