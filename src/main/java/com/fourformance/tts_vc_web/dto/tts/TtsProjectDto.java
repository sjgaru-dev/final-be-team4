package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class TtsProjectDto {

    private Long id;
    private String projectName;
    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;
    private APIStatusConst apiStatus;
    private LocalDateTime apiStatusModifiedAt;
    private Long styleId;

}
