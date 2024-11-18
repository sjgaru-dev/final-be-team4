package com.fourformance.tts_vc_web.dto.tts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TTSRequestDto {
    private String projectName;
    private Long voiceStyleId;
    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;
    private List<TTSDetailRequestDto> ttsDetails;
}
