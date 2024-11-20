package com.fourformance.tts_vc_web.service.concat;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.dto.concat.ConcatDetailDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatProjectDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatSaveDto;
import com.fourformance.tts_vc_web.repository.ConcatDetailRepository;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConcatService_team_aws {

    private final ConcatProjectRepository concatProjectRepository;
    private final ConcatDetailRepository concatDetailRepository;

    // Concat 프로젝트 조회
    @Transactional(readOnly = true)
    public ConcatProjectDto getConcatProjectDto(Long projectId) {
        ConcatProject concatProject = concatProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        return ConcatProjectDto.createFromEntity(concatProject);
    }

    // Concat 프로젝트 상세 값 조회
    @Transactional(readOnly = true)
    public List<ConcatDetailDto> getConcatDetailsDto(Long projectId) {
        List<ConcatDetail> concatDetails = concatDetailRepository.findByConcatProjectId(projectId);

        return concatDetails.stream()
                .filter(detail -> !detail.getIsDeleted())
                .map(ConcatDetailDto::createFromEntity)
                .collect(Collectors.toList());
    }

    // Concat 프로젝트 생성
    @Transactional
    public Long createNewProject(ConcatSaveDto dto) {
        // ConcatDetailDto의 audioSeq 중복 및 순차 검증
        if (dto.getConcatDetails() != null) {
            validateAudioSequence(dto.getConcatDetails());
        }

        // ConcatProject 생성
        ConcatProject concatProject = ConcatProject.createConcatProject(
                null,
                dto.getProjectName()
        );
        concatProject = concatProjectRepository.save(concatProject);

        // ConcatDetails 생성
        if (dto.getConcatDetails() != null) {
            for (ConcatDetailDto detailDto : dto.getConcatDetails()) {
                createConcatDetail(detailDto, concatProject);
            }
        }

        return concatProject.getId();
    }

    // Concat 프로젝트 업데이트
    @Transactional
    public Long updateProject(ConcatSaveDto dto) {
        ConcatProject concatProject = concatProjectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        // ConcatDetailDto의 audioSeq 중복 및 순차 검증
        if (dto.getConcatDetails() != null) {
            validateAudioSequence(dto.getConcatDetails());
        }

        // 프로젝트 이름 업데이트
        concatProject.updateConcatProject(
                dto.getProjectName(),
                dto.getGlobalFrontSilenceLength(),
                dto.getGlobalTotalSilenceLength()
        );

        // ConcatDetails 처리
        if (dto.getConcatDetails() != null) {
            for (ConcatDetailDto detailDto : dto.getConcatDetails()) {
                processConcatDetail(detailDto, concatProject);
            }
        }

        return concatProject.getId();
    }

    // ConcatDetail 생성
    private void createConcatDetail(ConcatDetailDto detailDto, ConcatProject concatProject) {
        ConcatDetail concatDetail = ConcatDetail.createConcatDetail(
                concatProject,
                detailDto.getAudioSeq(),
                true,
                detailDto.getUnitScript(),
                detailDto.getEndSilence()
        );

        concatDetailRepository.save(concatDetail);
    }

    // ConcatDetail 처리 (업데이트 or 생성)
    private void processConcatDetail(ConcatDetailDto detailDto, ConcatProject concatProject) {
        if (detailDto.getId() != null) {
            ConcatDetail concatDetail = concatDetailRepository.findById(detailDto.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT_DETAIL));
            concatDetail.updateDetails(
                    detailDto.getAudioSeq(),
                    true,
                    detailDto.getUnitScript(),
                    detailDto.getEndSilence(),
                    detailDto.getIsDeleted()
            );
            concatDetailRepository.save(concatDetail);
        } else {
            createConcatDetail(detailDto, concatProject);
        }
    }

    // ConcatDetail의 audioSeq 검증
    private void validateAudioSequence(List<ConcatDetailDto> detailDtos) {
        Set<Integer> audioSeqSet = new HashSet<>();

        for (ConcatDetailDto detailDto : detailDtos) {
            if (!audioSeqSet.add(detailDto.getAudioSeq())) {
                throw new BusinessException(ErrorCode.DUPLICATE_UNIT_SEQUENCE);
            }
        }

        List<Integer> sequences = detailDtos.stream()
                .map(ConcatDetailDto::getAudioSeq)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i) != i + 1) {
                throw new BusinessException(ErrorCode.INVALID_UNIT_SEQUENCE_ORDER);
            }
        }
    }
}
