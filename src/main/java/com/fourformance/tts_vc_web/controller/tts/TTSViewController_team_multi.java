package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.tts.*;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_multi;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tts")
@RequiredArgsConstructor
public class TTSViewController_team_multi {

    @Autowired
    TTSService_team_multi ttsService;


    // TTS 상태 로드 메서드
    @Operation(
            summary = "TTS 상태 로드",
            description = "TTS 프로젝트 상태를 가져옵니다." )
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
            description = "TTS 프로젝트 상태를 저장합니다." )
    @PostMapping("/{projectId}/save")
    public ResponseEntity<TtsStatusSaveResponseDto> save(@RequestBody TTSProjectWithDetailsDto ttsProjectDetailDto) {
        // service 호출 및 저장된 project ID 반환, 새 프로젝트(projectId=null) 저장 시 project ID를 생성함
        Long projectId = ttsService.saveTTSProjectAndDetail(ttsProjectDetailDto);

        // 성공 메시지와 projectId를 응답으로 반환
        TtsStatusSaveResponseDto response = new TtsStatusSaveResponseDto("Project saved successfully", projectId);
        return ResponseEntity.ok(response);
    }

}
