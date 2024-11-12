package com.fourformance.tts_vc_web.dto.tts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TtsDetailDto {
    private Long detailId; // TTSDetail의 ID (업데이트를 위한 필드)
    private Long detailStyleId; // TTSDetail의 Style ID
    private String unitScript;
    private Float unitSpeed;
    private Float unitPitch;
    private Float unitVolume;
    private Boolean isDeleted;
    private Integer unitSequence;
}
