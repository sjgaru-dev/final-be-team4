package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectWithDetailsDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_multi;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tts")
@RequiredArgsConstructor
public class TTSViewController_team_multi {

    @Autowired
    TTSService_team_multi ttsService;


    // TTS 상태 로드 메서드
    @Operation(
            summary = "TTS 상태 로드",
            description = "TTS 프로젝트 상태를 가져옵니다.")
    @GetMapping("/{projectId}")
    public ResponseDto ttsLoad(@PathVariable Long projectId) {
        try {
            // TTSProjectDTO와 TTSDetailDTO 리스트 가져오기
            TTSProjectDto ttsProjectDTO = ttsService.getTTSProjectDto(projectId);
            List<TTSDetailDto> ttsDetailsDTO = ttsService.getTTSDetailsDto(projectId);

            if (ttsProjectDTO == null) {
                throw new BusinessException(ErrorCode.NOT_EXISTS_PROJECT);
            }

            // DTO를 포함한 응답 객체 생성
            TTSProjectWithDetailsDto response = new TTSProjectWithDetailsDto(ttsProjectDTO, ttsDetailsDTO);
            return DataResponseDto.of(response);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }

    // TTS 상태 저장 메서드
    @Operation(
            summary = "TTS 상태 저장",
            description = "TTS 프로젝트 상태를 저장합니다.")
    @PostMapping("/{projectId}/save")
    public ResponseDto ttsSave(
            @RequestBody TTSSaveDto ttsSaveDto,
            HttpSession session) {
        try {

//            Long memberId = (Long) session.getAttribute("memberId");
            Long memberId = 1L; // 개발 단계 임시 하드코딩

            Long projectId;
            if (ttsSaveDto.getProjectId() == null) {
                // projectId가 null인 경우, 새 프로젝트 생성
                projectId = ttsService.createNewProject(ttsSaveDto, memberId);
            } else {
                // projectId가 존재하면, 기존 프로젝트 업데이트
                projectId = ttsService.updateProject(ttsSaveDto, memberId);
            }
            return DataResponseDto.of(projectId, "상태가 성공적으로 저장되었습니다.");
        } catch (BusinessException e) {
            throw e;  // 기존의 BusinessException 그대로 던짐
        }
    }

}
