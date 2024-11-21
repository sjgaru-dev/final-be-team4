package com.fourformance.tts_vc_web.service.concat;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.dto.concat.CNCTDetailDto;
import com.fourformance.tts_vc_web.dto.concat.CNCTProjectDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatAudioDto;
import com.fourformance.tts_vc_web.dto.vc.TrgAudioDto;
import com.fourformance.tts_vc_web.dto.vc.VCDetailResDto;
import com.fourformance.tts_vc_web.dto.vc.VCProjectDto;
import com.fourformance.tts_vc_web.dto.vc.VCProjectResDto;
import com.fourformance.tts_vc_web.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ConcatService_team_multi {

    ConcatProjectRepository concatProjectRepository;
    ConcatDetailRepository concatDetailRepository;
    OutputAudioMetaRepository outputAudioMetaRepository;
    MemberAudioMetaRepository memberAudioMetaRepository;
    MemberAudioConcatRepository memberAudioConcatRepository;

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
            // MemberAudioMeta ID 조회
            List<Long> memberAudioIds = memberAudioConcatRepository.findMemberAudioMetaByVcProjectId(cnctPrjDto.getId());

            // Audio URL과 ID 조회하여 TrgAudioDto 리스트 생성
            List<MemberAudioMeta> memberAudioMetaList = memberAudioMetaRepository.findByMemberAudioIds(
                    memberAudioIds, AudioType.CONCAT
            );

            // MemberAudioMeta를 ConcatAudioDto 변환
            concatAudioDtos = memberAudioMetaList.stream()
                    .map(meta -> new ConcatAudioDto(meta.getId(), meta.getAudioUrl()))
                    .toList();
        }

        // CNCTProjectDto 생성 및 반환
        CNCTProjectDto resDto = new CNCTProjectDto();
                       resDto.setId(cnctPrjDto.getId());
                       resDto.setProjectName(cnctPrjDto.getProjectName());
                       resDto.setGlobalFrontSilenceLength(cnctPrjDto.getGlobalFrontSilenceLength());
                       resDto.setGlobalTotalSilenceLength(cnctPrjDto.getGlobalTotalSilenceLength());
                       resDto.setConcatAudios(concatAudioDtos);

        return resDto;

    }

    // Concat 프로젝트 상세 값 조회하기
    @Transactional(readOnly = true)
    public List<CNCTDetailDto> getConcatDetailsDto(Long projectId) {
        List<ConcatDetail> cocnatDetails = concatDetailRepository.findByConcatProject_Id(projectId);

        // isDeleted가 false인 경우에만 VCDetailResDto 목록으로 변환
        return cocnatDetails.stream()
                .filter(detail -> !detail.getIsDeleted()) // isDeleted가 false인 경우
                .map(this::convertToVCDetailResDto) // VCDetailResDto로 변환
                .collect(Collectors.toList());
    }

    // ConcatDetail 엔티티를 CNCTDetailDto로 변환하는 메서드
    private CNCTDetailDto convertToVCDetailResDto(ConcatDetail concatDetail) {

        // src 오디오 url 추가하기
//        List<String> audioUrls = outputAudioMetaRepository.findAudioUrlsByConcatDetail(concatDetail.getId())
//                .stream() // List<OutputAudioMeta>를 Stream으로 변환
//                .filter(meta -> meta.getAudioUrl() != null) // audioUrl이 null이 아닌 경우만 필터링
//                .map(OutputAudioMeta::getAudioUrl) // OutputAudioMeta의 audioUrl만 추출
//                .collect(Collectors.toList()); // Stream 결과를 List<String>으로 변환
//
        CNCTDetailDto resDto = new CNCTDetailDto();
//                       resDto.setId(concatDetail.getId());
//                       resDto.setProjectId(concatDetail.getVcProject().getId());
//                       resDto.setIsChecked(concatDetail.getIsChecked());
//                       resDto.setUnitScript(concatDetail.getUnitScript());
//                       resDto.setGenAudios(audioUrls);
        return resDto;
    }

}
