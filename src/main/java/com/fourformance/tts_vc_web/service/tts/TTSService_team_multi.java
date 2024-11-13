package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectDto;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
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


    @Transactional(readOnly = true)
    public TTSProjectDto getTTSProjectDto(Long projectId) {
        // 프로젝트 조회
        TTSProject ttsProject = ttsProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // TTSProjectDTO로 변환
        return TTSProjectDto.fromEntity(ttsProject);
    }

    @Transactional(readOnly = true)
    public List<TTSDetailDto> getTTSDetailsDto(Long projectId) {
        List<TTSDetail> ttsDetails = ttsDetailRepository.findByTtsProjectId(projectId);

        // isDeleted가 false인 경우에만 TTSDetailDTO 목록으로 변환
        return ttsDetails.stream()
                .filter(detail -> !detail.getIsDeleted()) // isDeleted가 false인 경우만 필터링
                .map(TTSDetailDto::fromEntity) // TTSDetailDto로 변환
                .collect(Collectors.toList());
    }

}
