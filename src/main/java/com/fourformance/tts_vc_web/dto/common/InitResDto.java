package com.fourformance.tts_vc_web.dto.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InitResDto { // 초기 API 요청 시, 전달되는 값
    List<VoiceStyleDto> voiceStyleDto;

    public InitResDto(List<VoiceStyleDto> voiceStyleDto) {
        this.voiceStyleDto = voiceStyleDto;
    }
}
