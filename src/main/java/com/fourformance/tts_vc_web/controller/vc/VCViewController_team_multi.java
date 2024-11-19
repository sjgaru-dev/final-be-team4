package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.vc.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vc")
@RequiredArgsConstructor
public class VCViewController_team_multi {

//    @Autowired
//    VCService_team_multi vcService;

    // VC 상태 로드 메서드
    @Operation(
            summary = "VC 상태 로드",
            description = "VC 프로젝트 상태를 가져옵니다." )
    @GetMapping("/load")
    public ResponseDto ttsLoad(@RequestParam("projectId") Long projectId) {

//        try {
//            // VCProjectDTO와 VCDetailDTO 리스트 가져오기
//            VCProjectResDto vcProjectDTO = vcService.getVCProjectDto(projectId);
//            List<VCDetailResDto> vcDetailsDTO = vcService.getVCDetailsDto(projectId);
//
//            if (vcProjectDTO == null) {
//                throw new BusinessException(ErrorCode.NOT_EXISTS_PROJECT);
//            }
//
//            // DTO를 포함한 응답 객체 생성
//            VCProjectWithDetailResDto response = new VCProjectWithDetailResDto(vcProjectDTO, vcDetailsDTO);
//            return DataResponseDto.of(response);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.SERVER_ERROR);
//        }
        return DataResponseDto.of("");
    }

    // VC 상태 저장 메서드
    @Operation(
            summary = "VC 상태 로드",
            description = "VC 프로젝트 상태를 가져옵니다." )
    @PostMapping("/{projectId}/save")
    public ResponseDto vcSave(@RequestBody VCSaveDto vcSaveDto){
        return DataResponseDto.of("");
    }

}
