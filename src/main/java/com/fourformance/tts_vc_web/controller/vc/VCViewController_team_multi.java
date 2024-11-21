package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.vc.*;
import com.fourformance.tts_vc_web.service.common.ProjectService_team_multi;
import com.fourformance.tts_vc_web.service.vc.VCService_team_multi;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vc")
@RequiredArgsConstructor
public class VCViewController_team_multi {

    @Autowired
    ProjectService_team_multi projectService;

    @Autowired
    VCService_team_multi vcService;

    // VC 상태 로드 메서드
    @Operation(
            summary = "VC 상태 로드",
            description = "VC 프로젝트 상태를 가져옵니다." )
    @GetMapping("/{projectId}")
    public ResponseDto vcLoad(@PathVariable("projectId") Long projectId) {

        // VCProjectDTO와 VCDetailDTO 리스트 가져오기
        VCProjectResDto vcProjectDTO = vcService.getVCProjectDto(projectId);
        List<VCDetailResDto> vcDetailsDTO = vcService.getVCDetailsDto(projectId);

        if (vcProjectDTO == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_PROJECT);
        }

        try {
            // DTO를 포함한 응답 객체 생성
            VCProjectWithDetailResDto response = new VCProjectWithDetailResDto(vcProjectDTO, vcDetailsDTO);
            return DataResponseDto.of(response);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }

    }

    // VC 상태 저장 메서드
    @Operation(
            summary = "VC 상태 로드",
            description = "VC 프로젝트 상태를 가져옵니다." )
    @PostMapping("/{projectId}/save")
    public ResponseDto vcSave(@RequestBody VCSaveDto vcSaveDto){
        return DataResponseDto.of("");
    }


    // VC 프로젝트 삭제
    @Operation(
            summary = "VC 프로젝트 삭제",
            description = "VC 프로젝트와 생성된 오디오 등 관련된 데이터를 전부 삭제합니다." )
    @PostMapping("/delete/{projectId}")
    public ResponseDto deleteVCProject(@PathVariable("projectId") Long projectId) {
        // 타입 검증
        if(projectId == null) { throw new BusinessException(ErrorCode.INVALID_PROJECT_ID); }

        // 프로젝트 삭제
        projectService.deleteVCProject(projectId);

        // 작업 상태 : Terminated(종료)
        return DataResponseDto.of("","VC 프로젝트가 정상적으로 삭제되었습니다.");
    }

    // VC 선택된 모든 항목 삭제
    @Operation(
            summary = "VC 선택된 항목 삭제",
            description = "VC 프로젝트에서 선택된 모든 항목을 삭제합니다." )
    @PostMapping("/delete/details")
    public ResponseDto deleteVCDetail(@RequestBody List<Long> vcDetailsId) {
        return DataResponseDto.of("");
    }

    // TRG 오디오 선택된 모든 항목 삭제
    @Operation(
            summary = "VC 프로젝트 target 오디오 선택 항목 삭제",
            description = "VC 프로젝트에서 target 오디오 선택된 모든 항목을 삭제합니다." )
    @PostMapping("/delete/trg")
    public ResponseDto deleteTRGAudio(@RequestBody List<Long> targetAudioId) {
        return DataResponseDto.of("");
    }
}
