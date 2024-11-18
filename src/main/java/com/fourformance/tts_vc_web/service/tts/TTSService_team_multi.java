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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_VOICESTYLE));

        // TTSDetailDto 리스트에 대한 unitSequence 검증
        if (dto.getTtsDetails() != null) {
            validateUnitSequence(dto.getTtsDetails());
        }

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

        //tts 프로젝트 db에 저장
        ttsProject = ttsProjectRepository.save(ttsProject);

        // tts detail 저장
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_VOICESTYLE));

        // TTSDetailDto 리스트에 대한 unitSequence 검증
        if (dto.getTtsDetails() != null) {
            validateUnitSequence(dto.getTtsDetails());
        }

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
                // ttsDetail 업데이트 메서드 호출
                processTTSDetail(detailDto, ttsProject);
            }
        }
        return ttsProject.getId();
    }

    // ttsDetail 생성 메서드
    private void createTTSDetail(TTSDetailDto detailDto, TTSProject ttsProject) {
        VoiceStyle detailStyle = voiceStyleRepository.findById(detailDto.getVoiceStyleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_VOICESTYLE));

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
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL));
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

    /**
     * TTSDetailDto 리스트에서 unitSequence 값을 검증하는 메서드.
     * 중복된 unitSequence 값이 없는지, unitSequence가 순차적인지(1, 2, 3, ...) 확인합니다.
     *
     * @param detailDtos TTSDetailDto 리스트
     * @throws BusinessException DUPLICATE_UNIT_SEQUENCE 예외는 unitSequence에 중복이 있을 때 발생
     *                           INVALID_UNIT_SEQUENCE_ORDER 예외는 unitSequence가 순차적이지 않을 때 발생
     */
    private void validateUnitSequence(List<TTSDetailDto> detailDtos) {
        // 중복 체크를 위한 Set 생성
        Set<Integer> unitSequenceSet = new HashSet<>();

        // unitSequence 값 중복 여부 확인
        for (TTSDetailDto detailDto : detailDtos) {
            if (!unitSequenceSet.add(detailDto.getUnitSequence())) {
                // 중복된 unitSequence가 발견되면 예외 발생
                throw new BusinessException(ErrorCode.DUPLICATE_UNIT_SEQUENCE);
            }
        }

        // unitSequence 값을 정렬된 리스트로 변환하여 순차 여부 확인
        List<Integer> sequences = detailDtos.stream()
                .map(TTSDetailDto::getUnitSequence)
                .sorted()
                .collect(Collectors.toList());

        // 정렬된 unitSequence 리스트가 [1, 2, 3, ...] 순서인지 확인
        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i) != i + 1) {
                // 순서가 맞지 않는 경우 예외 발생
                throw new BusinessException(ErrorCode.INVALID_UNIT_SEQUENCE_ORDER);
            }
        }
    }

}
