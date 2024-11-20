package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TTSSaveDto {
    private Long projectId;
    private String projectName;
    private Long voiceStyleId;
    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;
    private APIStatusConst apiStatus;
    private Long memberId; // 연관된 Member ID -- 원우 추가

    private List<TTSDetailDto> ttsDetails;
}
