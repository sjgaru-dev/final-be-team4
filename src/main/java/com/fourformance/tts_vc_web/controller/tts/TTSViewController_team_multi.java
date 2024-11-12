package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.dto.tts.TtsProjectDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TtsStatusSaveResponseDto;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_multi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tts")
@RequiredArgsConstructor
public class TTSViewController_team_multi {

    @Autowired
    TTSProjectRepository ttsProjectRepository;

    @Autowired
    TTSDetailRepository ttsDetailRepository;

    @Autowired
    TTSService_team_multi ttsService_team_multi;


    // TTS 상태 저장 메서드
    @PostMapping("/{projectId}/save")
    public ResponseEntity<TtsStatusSaveResponseDto> save(@RequestBody TtsProjectDetailDto ttsProjectDetailDto) {
        // service 호출 및 저장된 project ID 반환, 새 프로젝트(projectId=null) 저장 시 project ID를 생성함
        Long projectId = ttsService_team_multi.saveTTSProjectAndDetail(ttsProjectDetailDto);

        // 성공 메시지와 projectId를 응답으로 반환
        TtsStatusSaveResponseDto response = new TtsStatusSaveResponseDto("Project saved successfully", projectId);
        return ResponseEntity.ok(response);
    }

}
