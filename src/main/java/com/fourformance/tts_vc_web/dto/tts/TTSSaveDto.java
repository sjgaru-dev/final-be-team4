package com.fourformance.tts_vc_web.dto.tts;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TTSSaveDto {
    private Long projectId;
    //    private Long memberId;
    private String projectName;
    private Long globalVoiceStyleId;
    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;
//    private APIStatusConst apiStatus;

    private List<TTSDetailDto> ttsDetails;
}
