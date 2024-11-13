package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectWithDetailsDto;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_multi;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tts")
@RequiredArgsConstructor
public class TTSViewController_team_multi {

    private final TTSService_team_multi ttsService;

    // TTS 상태 로드 메서드
    @Operation(
            summary = "TTS 상태 로드",
            description = "TTS 프로젝트 상태를 가져옵니다." )
    @GetMapping("/{projectId}")
    public ResponseEntity<TTSProjectWithDetailsDto> getTTSProjectWithDetails(@PathVariable Long projectId) {
        // TTSProjectDTO와 TTSDetailDTO 리스트 가져오기
        TTSProjectDto ttsProjectDTO = ttsService.getTTSProjectDto(projectId);
        List<TTSDetailDto> ttsDetailsDTO = ttsService.getTTSDetailsDto(projectId);

        // DTO를 포함한 응답 객체 생성
        TTSProjectWithDetailsDto response = new TTSProjectWithDetailsDto(ttsProjectDTO, ttsDetailsDTO);

        return ResponseEntity.ok(response);
    }

    // TTS 생성된 오디오 내역 메서드

}
