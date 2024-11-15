package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TTSSaveDto {
    private Long projectId;
    private String projectName;
    private Long voiceStyleId;
    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;
    private APIStatusConst apiStatus;

    private List<TTSDetailDto> ttsDetails;
}
