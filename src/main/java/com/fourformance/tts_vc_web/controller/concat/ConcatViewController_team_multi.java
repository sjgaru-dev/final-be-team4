package com.fourformance.tts_vc_web.controller.concat;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.concat.CNCTDetailDto;
import com.fourformance.tts_vc_web.dto.concat.CNCTProjectDto;
import com.fourformance.tts_vc_web.dto.concat.CNCTProjectWithDetailDto;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.concat.ConcatService_team_multi;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/concat")
@RequiredArgsConstructor
public class ConcatViewController_team_multi {

    @Autowired
    ConcatService_team_multi concatService;


    // Concat 상태 로드 메서드
    @Operation(
            summary = "Concat 상태 로드",
            description = "Concat 프로젝트 상태를 가져옵니다." )
    @GetMapping("/{projectId}")
    public ResponseDto concatLoad(@PathVariable("projectId") Long projectId) {

        // CNCTProjectDTO와 CNCTDetailDTO 리스트 가져오기
        CNCTProjectDto cnctProjectDTO = concatService.getConcatProjectDto(projectId);
        List<CNCTDetailDto> cnctDetailsDTO = concatService.getConcatDetailsDto(projectId);

        if (cnctProjectDTO == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_PROJECT);
        }

        try {
            // DTO를 포함한 응답 객체 생성
            CNCTProjectWithDetailDto response = new CNCTProjectWithDetailDto(cnctProjectDTO, cnctDetailsDTO);
            return DataResponseDto.of(response);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }


}
