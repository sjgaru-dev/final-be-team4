package com.fourformance.tts_vc_web.dto.tts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TTSDetailRequestDto {
    private Long id; // 기존 데이터의 식별자 (DB에서 가져온 경우 필요)
    private String unitScript;
    private Float unitSpeed;
    private Float unitPitch;
    private Float unitVolume;
    private Integer unitSequence;
    private Boolean isNew; // true: 새로운 데이터, false: 기존 데이터
    private Long voiceStyleId; // voiceStyleId (DB에서 가져온 경우 필요)

    private String projectName; // 파일명을 짓기 위해 projectName 선언

}
