package com.fourformance.tts_vc_web.service.concat;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.ConcatDetail;
import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.dto.concat.CNCTDetailDto;
import com.fourformance.tts_vc_web.dto.concat.CNCTProjectDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatAudioDto;
import com.fourformance.tts_vc_web.repository.ConcatDetailRepository;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioConcatRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConcatService_team_multi {

    private final ConcatProjectRepository concatProjectRepository;
    private final ConcatDetailRepository concatDetailRepository;
    private final OutputAudioMetaRepository outputAudioMetaRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final MemberAudioConcatRepository memberAudioConcatRepository;

    // Concat 프로젝트 상태 조회하기
    @Transactional(readOnly = true)
    public CNCTProjectDto getConcatProjectDto(Long projectId) {

        // 프로젝트 조회
        ConcatProject cnctProject = concatProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        // VCProjectDto로 변환
        CNCTProjectDto cnctPrjDto = CNCTProjectDto.createCNCTProjectDto(cnctProject);

        List<ConcatAudioDto> concatAudioDtos = null; // 변경된 구조에 맞는 변수 선언
        if (cnctPrjDto.getId() != null) {
            List<OutputAudioMeta> outputAudioList = outputAudioMetaRepository.findAudioUrlsByConcatProject(
                    cnctPrjDto.getId());
            // MemberAudioMeta를 ConcatAudioDto 변환
            concatAudioDtos = outputAudioList.stream()
                    .map(meta -> new ConcatAudioDto(meta.getId(), meta.getAudioUrl()))
                    .toList();
        }

        // CNCTProjectDto 생성 및 반환
        CNCTProjectDto resDto = new CNCTProjectDto();
        resDto.setId(cnctPrjDto.getId());
        resDto.setProjectName(cnctPrjDto.getProjectName());
        resDto.setGlobalFrontSilenceLength(cnctPrjDto.getGlobalFrontSilenceLength());
        resDto.setGlobalTotalSilenceLength(cnctPrjDto.getGlobalTotalSilenceLength());
        resDto.setConcatAudios(concatAudioDtos); // Concat 생성된 오디오

        return resDto;

    }

    // Concat 프로젝트 상세 값 조회하기
    @Transactional(readOnly = true)
    public List<CNCTDetailDto> getConcatDetailsDto(Long projectId) {
        List<ConcatDetail> cocnatDetails = concatDetailRepository.findByConcatProject_Id(projectId);

        // isDeleted가 false인 경우에만 VCDetailResDto 목록으로 변환
        return cocnatDetails.stream()
                .filter(detail -> !detail.getIsDeleted()) // isDeleted가 false인 경우
                .map(this::convertToCNCTDetailDto) // CNCTDetailDto로 변환
                .collect(Collectors.toList());
    }

    // ConcatDetail 엔티티를 CNCTDetailDto로 변환하는 메서드
    private CNCTDetailDto convertToCNCTDetailDto(ConcatDetail concatDetail) {

        // src 오디오 url 추가하기
        MemberAudioMeta memberAudioMeta = memberAudioMetaRepository.findByIdAndAudioType(
                concatDetail.getMemberAudioMeta().getId(), AudioType.CONCAT);

        CNCTDetailDto resDto = new CNCTDetailDto();
        resDto.setId(concatDetail.getId());
        resDto.setAudioSeq(concatDetail.getAudioSeq());
        resDto.setSrcUrl(memberAudioMeta.getAudioUrl());
        resDto.setChecked(concatDetail.isChecked());
        resDto.setUnitScript(concatDetail.getUnitScript());
        resDto.setEndSilence(concatDetail.getEndSilence());

        return resDto;
    }

}
