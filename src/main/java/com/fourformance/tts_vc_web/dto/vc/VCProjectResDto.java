package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VCProjectResDto {

    private Long id; // 프로젝트 ID
    private String projectName; // 프로젝트 이름
    private String trgAudio; // 최근 업로드한 target 오디오 url
    private String voiceId; // 타겟 오디오 ID
}
