package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.dto.tts.TtsProjectDto;
import com.fourformance.tts_vc_web.dto.tts.TtsProjectDto;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tts")
@RequiredArgsConstructor
public class TTSViewController_team_multi {

    @Autowired
    TTSProjectRepository ttsProjectRepository;

    @Autowired
    TTSDetailRepository ttsDetailRepository;

    // TTS 상태 로드 메서드
    @GetMapping("/{projectId}")
    public String load(Model m){
        return "projectLoad";
    }

    // TTS 상태 저장 메서드
    @PostMapping("/{projectId}/save")
    public String save(@RequestBody TtsProjectDto ttsDto){
        return "projectSave";
    }

    // TTS 상태 저장 메서드
    @PostMapping("/{projectId}/saveTest")
    public String saveTest(@RequestBody TtsProjectDto ttsDto){
        return "projectSave";
    }

}
