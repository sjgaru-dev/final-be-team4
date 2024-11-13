package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TtsProjectDetailDto {

    // Project 관련 필드
    private Long projectId;
    private String projectName; // project_id가 있는데 굳이 project_name이 필요할까?

    // TTSProject 관련 필드
    private Long styleId; //TTSProject의 Style ID
    private String fullScript;
    private Float globalSpeed;
    private Float globalPitch;
    private Float globalVolume;

    // 여러 개의 TTSDetail 관련 필드를 리스트로 구성
    private List<TtsDetailDto> ttsDetails;

}
