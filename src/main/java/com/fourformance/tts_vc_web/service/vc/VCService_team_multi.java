package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.vc.*;
import com.fourformance.tts_vc_web.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class VCService_team_multi {
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;

    private final OutputAudioMetaRepository outputAudioMetaRepository;
    private final MemberAudioVCRepository memberAudioVCRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;

    // VC 프로젝트 상태 조회하기
    @Transactional(readOnly = true)
    public VCProjectResDto getVCProjectDto(Long projectId) {
        // 프로젝트 조회
        VCProject vcProject = vcProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        // VCProjectDto로 변환
        VCProjectDto vcPrjDto = VCProjectDto.createVCProjectDto(vcProject);

        List<TrgAudioDto> trgAudioDtos = null; // 변경된 구조에 맞는 변수 선언
        if (vcPrjDto.getId() != null) {
            // MemberAudioMeta ID 조회
            List<Long> memberAudioIds = memberAudioVCRepository.findMemberAudioMetaByVcProjectId(vcPrjDto.getId());

            // Audio URL과 ID 조회하여 TrgAudioDto 리스트 생성
            List<MemberAudioMeta> memberAudioMetaList = memberAudioMetaRepository.findByMemberAudioIds(
                    memberAudioIds, AudioType.VC_TRG
            );

            // MemberAudioMeta를 TrgAudioDto로 변환
            trgAudioDtos = memberAudioMetaList.stream()
                    .map(meta -> new TrgAudioDto(meta.getId(), meta.getAudioUrl()))
                    .toList();
        }

        // VCProjectResDto 생성 및 반환
        VCProjectResDto resDto = new VCProjectResDto();
                        resDto.setId(vcPrjDto.getId());
                        resDto.setProjectName(vcPrjDto.getProjectName());
                        resDto.setTrgAudios(trgAudioDtos); // 새로운 구조 반영

        return resDto;
    }



    // VC 프로젝트 상세 값 조회하기
    @Transactional(readOnly = true)
    public List<VCDetailResDto> getVCDetailsDto(Long projectId) {
        List<VCDetail> vcDetails = vcDetailRepository.findByVcProject_Id(projectId);

        // isDeleted가 false인 경우에만 VCDetailResDto 목록으로 변환
        return vcDetails.stream()
                .filter(detail -> !detail.getIsDeleted()) // isDeleted가 false인 경우
                .map(this::convertToVCDetailResDto) // VCDetailResDto로 변환
                .collect(Collectors.toList());
    }

    // VCDetail 엔티티를 VCDetailResDto로 변환하는 메서드
    private VCDetailResDto convertToVCDetailResDto(VCDetail vcDetail) {

        // src 오디오 url 추가하기
        List<String> audioUrls = outputAudioMetaRepository.findAudioUrlsByVcDetail(vcDetail.getId())
                .stream() // List<OutputAudioMeta>를 Stream으로 변환
                .filter(meta -> meta.getAudioUrl() != null) // audioUrl이 null이 아닌 경우만 필터링
                .map(OutputAudioMeta::getAudioUrl) // OutputAudioMeta의 audioUrl만 추출
                .collect(Collectors.toList()); // Stream 결과를 List<String>으로 변환

        VCDetailResDto resDto = new VCDetailResDto();
                       resDto.setId(vcDetail.getId());
                       resDto.setProjectId(vcDetail.getVcProject().getId());
                       resDto.setIsChecked(vcDetail.getIsChecked());
                       resDto.setUnitScript(vcDetail.getUnitScript());
                       resDto.setGenAudios(audioUrls);
        return resDto;
    }

}
